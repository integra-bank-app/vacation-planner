package integra.domain.controller;
import integra.domain.dto.FlightRequest;
import integra.domain.dto.FlightResponse;
import integra.domain.model.Flight;
import integra.domain.service.FlightService;
import org.springframework.web.bind.annotation.*;
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