package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.dto.TripRestaurantRequestDTO;
import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;
import integra.vacation_planner_backend.service.TripRestaurantService;
import integra.vacation_planner_backend.service.TripService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;

    private final TripRestaurantService tripRestaurantService;

    public TripController(
        TripService tripService,
        TripRestaurantService tripRestaurantService
    ) {
        this.tripService = tripService;
        this.tripRestaurantService = tripRestaurantService;
    }

    @PostMapping
    public UUID createTrip(@RequestBody TripDTO dto) {
        return tripService.createTrip(dto);
    }

    @GetMapping
    public List<Trip> getTrips(
            @RequestParam String username,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return tripService.getTripsBetweenDates(username, startDate, endDate);
    }

    @PutMapping("/{tripId}")
    public void updateTrip(@PathVariable UUID tripId, @RequestBody TripDTO dto) {
        tripService.updateTrip(tripId, dto);
    }

    @DeleteMapping("/{tripId}")
    public void deleteTrip(@PathVariable UUID tripId, @RequestParam String username) {
        tripService.deleteTrip(tripId, username);
    }

    @PostMapping("/{tripId}/restaurants")
    public UUID addRestaurantToTrip(
        @PathVariable UUID tripId,
        @RequestBody TripRestaurantRequestDTO request
    ) {
        return tripRestaurantService.addRestaurantToTrip(
            tripId,
            request.username(),
            request.restaurantId(),
            request.visitDate(),
            request.visitTime()
        );
    }

    @GetMapping("/{tripId}/restaurants")
    public List<TripRestaurantResponseDTO> getRestaurantsForTrip(
        @PathVariable UUID tripId,
        @RequestParam String username
    ) {
        return tripRestaurantService.getRestaurantsForTrip(tripId, username);
    }

    @DeleteMapping("/{tripId}/restaurants/{visitId}")
    public void removeRestaurantFromTrip(
        @PathVariable UUID tripId,
        @PathVariable UUID visitId,
        @RequestParam String username
    ) {
        tripRestaurantService.removeRestaurantFromTrip(
            tripId, visitId, username
        );
    }
}
