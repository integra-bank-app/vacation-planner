package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.AccommodationRequest;
import integra.vacation_planner_backend.dto.AccommodationResponse;
import integra.vacation_planner_backend.service.AccommodationService;
import integra.vacation_planner_backend.dto.UpdatePriceRequest;
import integra.vacation_planner_backend.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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

import java.util.UUID;

@RestController
@RequestMapping("/accommodations")
public class AccommodationController {

    private final AccommodationService service;

    public AccommodationController(AccommodationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UUID> create(
            @RequestBody AccommodationRequest request
    ) {
        UUID id = service.create(request);

        return ResponseEntity.ok(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePrice(
            @PathVariable UUID id,
            @RequestBody UpdatePriceRequest request
    ) {
        service.updatePrice(id, request.pricePerNight());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
        summary = "Search accommodations by city",
        description = "Results are sorted by name ascending, then id ascending."
    )
    public ResponseEntity<PageResponse<AccommodationResponse>> findByCity(
        @RequestParam String city,
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
        return ResponseEntity.ok(
            service.findByCity(city, page, size)
        );
    }
}
