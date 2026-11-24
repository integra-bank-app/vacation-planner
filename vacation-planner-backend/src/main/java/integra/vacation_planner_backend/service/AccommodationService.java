package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.dto.PageResponse;
import integra.vacation_planner_backend.mapper.AccommodationMapper;
import integra.vacation_planner_backend.mapper.AccommodationPageResponseMapper;
import integra.vacation_planner_backend.pagination.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import integra.vacation_planner_backend.dto.AccommodationRequest;
import integra.vacation_planner_backend.dto.AccommodationResponse;
import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.exception.AccommodationNotFoundException;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccommodationService {

    private final AccommodationRepository repository;

    private final AccommodationMapper accommodationMapper;

    private final AccommodationPageResponseMapper accommodationPageResponseMapper;

    public AccommodationService(AccommodationRepository repository, AccommodationMapper accommodationMapper, AccommodationPageResponseMapper accommodationPageResponseMapper) {
        this.repository = repository;
        this.accommodationMapper = accommodationMapper;
        this.accommodationPageResponseMapper = accommodationPageResponseMapper;
    }

    public UUID create(AccommodationRequest request) {

        validatePrice(request.pricePerNight());

        Accommodation accommodation = accommodationMapper.toAccommodation(request);

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

    public PageResponse<AccommodationResponse> findByCity(
        String city,
        int page,
        int size
    ) {
        PageRequest pageable = Pagination.create(
            page,
            size,
            Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
            )
        );

        Page<AccommodationResponse> accommodations = repository
            .findByCityIgnoreCase(city, pageable)
            .map(accommodationMapper::toResponse);

        return accommodationPageResponseMapper.toPageResponse(accommodations);
    }

    private void validatePrice(BigDecimal price) {

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than 0"
            );
        }
    }
}
