package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.service.FlightService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping
    public Flight createFlight(@RequestBody FlightRequest request) {
        return flightService.createFlight(request);
    }

    @PutMapping
    public Flight updateFlight(@RequestBody FlightRequest request) {
        return flightService.updateFlight(request);
    }

    @GetMapping
    public List<FlightResponse> getFlights(@RequestParam String airportCode) {
        return flightService.getFlights(airportCode);
    }

    @DeleteMapping
    public void deleteFlight(@RequestParam String flightNumber) {
        flightService.deleteFlight(flightNumber);
    }
}
