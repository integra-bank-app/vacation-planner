package integra.vacation_planner_backend.accommodation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accommodations")
public class AccommodationController {

    private final AccommodationService service;

    public AccommodationController(AccommodationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Long> create(
            @RequestBody AccommodationRequest request
    ) {
        Long id = service.create(request);

        return ResponseEntity.ok(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePrice(
            @PathVariable Long id,
            @RequestBody UpdatePriceRequest request
    ) {
        service.updatePrice(id, request.getPricePerNight());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
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