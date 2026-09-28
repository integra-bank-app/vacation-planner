package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.dto.AccommodationRequest;
import integra.vacation_planner_backend.dto.AccommodationResponse;
import integra.vacation_planner_backend.entity.Accommodation;
import integra.vacation_planner_backend.exception.AccommodationNotFoundException;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccommodationService {

    private final AccommodationRepository repository;

    public AccommodationService(AccommodationRepository repository) {
        this.repository = repository;
    }

    public UUID create(AccommodationRequest request) {

        validatePrice(request.pricePerNight());

        Accommodation accommodation = new Accommodation(
                request.name(),
                request.pricePerNight(),
                request.address(),
                request.city()
        );

        Accommodation saved = repository.save(accommodation);

        return saved.getId();
    }

    public void updatePrice(UUID id, BigDecimal newPrice) {

        validatePrice(newPrice);

        Accommodation accommodation = repository.findById(id)
                .orElseThrow(() ->
                        new AccommodationNotFoundException  (id)
                );

        accommodation.setPricePerNight(newPrice);

        repository.save(accommodation);
    }

    public void delete(UUID id) {

        if (!repository.existsById(id)) {
            throw new AccommodationNotFoundException(id);
        }

        repository.deleteById(id);
    }

    public List<AccommodationResponse> findByCity(String city) {

        return repository
                .findByCityIgnoreCaseOrderByPricePerNightAsc(city)
                .stream()
                .map(accommodation ->
                        new AccommodationResponse(
                                accommodation.getId(),
                                accommodation.getName(),
                                accommodation.getPricePerNight()
                        )
                )
                .toList();
    }

    private void validatePrice(BigDecimal price) {

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than 0"
            );
        }
    }
}