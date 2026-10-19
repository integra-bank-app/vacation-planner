package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.AddFlightToTripRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.service.TripService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("{tripId}/flights")
    public ResponseEntity<Void> addFlightToTrip(
        @PathVariable UUID tripId,
        @RequestBody AddFlightToTripRequest request
    ) {
        tripService.addFlightToTrip(
            tripId,
            request.getUsername(),
            request.getFlightNumber()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Trip> getTrips(
            @RequestParam String username,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return tripService.getTripsBetweenDates(username, startDate, endDate);
    }

    @GetMapping("/{tripId}/flights")
    public List<FlightResponse> getFlights(
        @PathVariable UUID tripId,
        @RequestParam String username
    ) {
        return tripService.getTripFlights(tripId, username);
    }

    @PutMapping("/{tripId}")
    public void updateTrip(@PathVariable UUID tripId, @RequestBody TripDTO dto) {
        tripService.updateTrip(tripId, dto);
    }

    @DeleteMapping("/{tripId}")
    public void deleteTrip(@PathVariable UUID tripId, @RequestParam String username) {
        tripService.deleteTrip(tripId, username);
    }
}
