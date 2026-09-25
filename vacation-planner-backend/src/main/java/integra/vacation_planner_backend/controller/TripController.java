package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.service.TripService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public Long createTrip( @RequestBody TripDTO dto) {
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
    public void updateTrip(@PathVariable Long tripId, @RequestBody TripDTO dto) {
        tripService.updateTrip(tripId, dto);
    }

    @DeleteMapping("/{tripId}")
    public void deleteTrip(@PathVariable Long tripId, @RequestParam String username) {
        tripService.deleteTrip(tripId, username);
    }
}