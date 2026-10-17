package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.dto.PageResponse;
import integra.vacation_planner_backend.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @Operation(
        summary = "Search flights by airport",
        description = "Results are sorted by departureTime ascending, then id ascending."
    )
    public PageResponse<FlightResponse> getFlights(
        @RequestParam String airportCode,
        @Parameter(
            description = "Page number, starting at 0",
            schema = @Schema(
                type = "integer",
                minimum = "0",
                defaultValue = "0"
            )
        )
        @RequestParam(defaultValue = "0") int page,
        @Parameter(
            description = "Number of items per page",
            schema = @Schema(
                type = "integer",
                defaultValue = "10",
                allowableValues = {"5", "10", "20", "50", "100"}
            )
        )
        @RequestParam(defaultValue = "10") int size
    ) {
        return flightService.getFlights(airportCode, page, size);
    }

    @DeleteMapping
    public void deleteFlight(@RequestParam String flightNumber) {
        flightService.deleteFlight(flightNumber);
    }
}
