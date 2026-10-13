package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.dto.TripResponse;
import integra.vacation_planner_backend.service.TripService;
import integra.vacation_planner_backend.dto.TripAccommodationRequest;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
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

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public UUID createTrip(@RequestBody TripDTO dto) {
        return tripService.createTrip(dto);
    }

    @GetMapping
    public List<TripResponse> getTrips(
        @RequestParam String username,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        return tripService.getTripsBetweenDates(username, startDate, endDate)
            .stream()
            .map(trip -> new TripResponse(
                trip.getId(),
                trip.getUsername(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate()
            ))
            .toList();
    }

    @PutMapping("/{tripId}")
    public void updateTrip(@PathVariable UUID tripId, @RequestBody TripDTO dto) {
        tripService.updateTrip(tripId, dto);
    }

    @DeleteMapping("/{tripId}")
    public void deleteTrip(@PathVariable UUID tripId, @RequestParam String username) {
        tripService.deleteTrip(tripId, username);
    }

    @PostMapping("/{tripId}/accommodations")
    public void addAccommodationToTrip(
        @PathVariable UUID tripId,
        @RequestBody TripAccommodationRequest request
    ) {
        tripService.addAccommodationToTrip(tripId, request);
    }

    @GetMapping("/{tripId}/accommodations")
    public List<TripAccommodationResponse> getTripAccommodations(
        @PathVariable UUID tripId,
        @RequestParam String username
    ) {
        return tripService.getTripAccommodations(tripId, username);
    }

    @DeleteMapping("/{tripId}/accommodations/{accommodationId}")
    public void removeAccommodationFromTrip(
        @PathVariable UUID tripId,
        @PathVariable UUID accommodationId,
        @RequestParam String username
    ) {
        tripService.removeAccommodationFromTrip(tripId, accommodationId, username);
    }
}
