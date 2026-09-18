package integra.vacation_planner_backend.accommodation;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccommodationService {

    private final AccommodationRepository repository;

    public AccommodationService(AccommodationRepository repository) {
        this.repository = repository;
    }

    public Long create(AccommodationRequest request) {

        validatePrice(request.getPricePerNight());

        Accommodation accommodation = new Accommodation(
                request.getName(),
                request.getPricePerNight(),
                request.getAddress(),
                request.getCity()
        );

        Accommodation saved = repository.save(accommodation);

        return saved.getId();
    }

    public void updatePrice(Long id, BigDecimal newPrice) {

        validatePrice(newPrice);

        Accommodation accommodation = repository.findById(id)
                .orElseThrow(() ->
                        new AccommodationNotFoundException(id)
                );

        accommodation.setPricePerNight(newPrice);

        repository.save(accommodation);
    }

    public void delete(Long id) {

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