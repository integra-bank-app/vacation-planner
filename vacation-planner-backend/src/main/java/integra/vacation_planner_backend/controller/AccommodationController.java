package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.AccommodationRequest;
import integra.vacation_planner_backend.dto.AccommodationResponse;
import integra.vacation_planner_backend.service.AccommodationService;
import integra.vacation_planner_backend.dto.UpdatePriceRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<List<AccommodationResponse>> findByCity(
            @RequestParam String city
    ) {
        return ResponseEntity.ok(
                service.findByCity(city)
        );
    }
}