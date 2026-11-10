package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.AddFlightToTripRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.dto.TripResponse;
import integra.vacation_planner_backend.dto.TripRestaurantRequestDTO;
import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;
import integra.vacation_planner_backend.service.TripRestaurantService;
import integra.vacation_planner_backend.service.TripService;
import integra.vacation_planner_backend.dto.TripAccommodationRequest;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
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
import integra.vacation_planner_backend.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.time.LocalDate;
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
    @Operation(
        summary = "Search trips by username and dates",
        description = "Results are sorted by startDate ascending, then id ascending."
    )
    public PageResponse<TripResponse> getTrips(
        @RequestParam String username,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
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
        return tripService.getTripsBetweenDates(
            username,
            startDate,
            endDate,
            page,
            size
        );
    }

    @PutMapping("/{tripId}")
    public void updateTrip(@PathVariable UUID tripId, @RequestBody TripDTO dto) {
        tripService.updateTrip(tripId, dto);
    }

    @DeleteMapping("/{tripId}")
    public void deleteTrip(@PathVariable UUID tripId, @RequestParam String username) {
        tripService.deleteTrip(tripId, username);
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

    @GetMapping("/{tripId}/flights")
    public PageResponse<FlightResponse> getFlights(
        @PathVariable UUID tripId,
        @RequestParam String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return tripService.getTripFlights(tripId, username, page, size);
    }

    @DeleteMapping("/{tripId}/flights/{flightNumber}")
    public void deleteTripFlight(
        @PathVariable UUID tripId,
        @PathVariable String flightNumber,
        @RequestParam String username
    ) {
        tripService.deleteFlightFromTrip(tripId, flightNumber, username);
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
    public PageResponse<TripRestaurantResponseDTO> getRestaurantsForTrip(
        @PathVariable UUID tripId,
        @RequestParam String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return tripRestaurantService.getRestaurantsForTrip(
            tripId, username, page, size
        );
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

    @PostMapping("/{tripId}/accommodations")
    public void addAccommodationToTrip(
        @PathVariable UUID tripId,
        @RequestBody TripAccommodationRequest request
    ) {
        tripService.addAccommodationToTrip(tripId, request);
    }

    @GetMapping("/{tripId}/accommodations")
    @Operation(
        summary = "List accommodations of a trip",
        description = "Results are sorted by name ascending, then id ascending."
    )
    public PageResponse<TripAccommodationResponse> getTripAccommodations(
        @PathVariable UUID tripId,
        @RequestParam String username,
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
        return tripService.getTripAccommodations(tripId, username, page, size);
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
