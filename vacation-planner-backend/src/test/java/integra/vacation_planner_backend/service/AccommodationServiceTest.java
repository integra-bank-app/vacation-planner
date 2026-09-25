package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.dto.AccommodationRequest;
import integra.vacation_planner_backend.dto.AccommodationResponse;
import integra.vacation_planner_backend.entity.Accommodation;
import integra.vacation_planner_backend.exception.AccommodationNotFoundException;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {

    @Mock
    private AccommodationRepository repository;

    @InjectMocks
    private AccommodationService service;

    private UUID accommodationId;

    @BeforeEach
    void setUp() {
        accommodationId = UUID.randomUUID();
    }

    @Test
    void create_shouldCreateAccommodation() {
        AccommodationRequest request = new AccommodationRequest(
                "Hotel Central",
                BigDecimal.valueOf(250),
                "Str. Memorandumului 10",
                "Cluj"
        );

        Accommodation savedAccommodation = new Accommodation(
                accommodationId,
                "Hotel Central",
                BigDecimal.valueOf(250),
                "Str. Memorandumului 10",
                "Cluj"
        );

        when(repository.save(any(Accommodation.class)))
                .thenReturn(savedAccommodation);

        UUID result = service.create(request);

        assertEquals(accommodationId, result);

        verify(repository).save(any(Accommodation.class));
    }

    @Test
    void create_shouldThrowException_whenPriceIsZero() {
        AccommodationRequest request = new AccommodationRequest(
                "Hotel Central",
                BigDecimal.ZERO,
                "Str. Memorandumului 10",
                "Cluj"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenPriceIsNegative() {
        AccommodationRequest request = new AccommodationRequest(
                "Hotel Central",
                BigDecimal.valueOf(-100),
                "Str. Memorandumului 10",
                "Cluj"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void create_shouldThrowException_whenPriceIsNull() {
        AccommodationRequest request = new AccommodationRequest(
                "Hotel Central",
                null,
                "Str. Memorandumului 10",
                "Cluj"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void updatePrice_shouldUpdatePrice() {
        Accommodation accommodation = new Accommodation(
                accommodationId,
                "Hotel Central",
                BigDecimal.valueOf(250),
                "Str. Memorandumului 10",
                "Cluj"
        );

        when(repository.findById(accommodationId))
                .thenReturn(Optional.of(accommodation));

        service.updatePrice(
                accommodationId,
                BigDecimal.valueOf(300)
        );

        assertEquals(
                BigDecimal.valueOf(300),
                accommodation.getPricePerNight()
        );

        verify(repository).findById(accommodationId);
        verify(repository).save(accommodation);
    }

    @Test
    void updatePrice_shouldThrowException_whenAccommodationDoesNotExist() {
        when(repository.findById(accommodationId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccommodationNotFoundException.class,
                () -> service.updatePrice(
                        accommodationId,
                        BigDecimal.valueOf(300)
                )
        );

        verify(repository).findById(accommodationId);
        verify(repository, never()).save(any());
    }

    @Test
    void updatePrice_shouldThrowException_whenPriceIsInvalid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updatePrice(
                        accommodationId,
                        BigDecimal.ZERO
                )
        );

        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteAccommodation() {
        when(repository.existsById(accommodationId))
                .thenReturn(true);

        service.delete(accommodationId);

        verify(repository).existsById(accommodationId);
        verify(repository).deleteById(accommodationId);
    }

    @Test
    void delete_shouldThrowException_whenAccommodationDoesNotExist() {
        when(repository.existsById(accommodationId))
                .thenReturn(false);

        assertThrows(
                AccommodationNotFoundException.class,
                () -> service.delete(accommodationId)
        );

        verify(repository).existsById(accommodationId);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void findByCity_shouldReturnAccommodationResponses() {
        Accommodation accommodation = new Accommodation(
                accommodationId,
                "Hotel Central",
                BigDecimal.valueOf(250),
                "Str. Memorandumului 10",
                "Cluj"
        );

        when(repository.findByCityIgnoreCaseOrderByPricePerNightAsc("Cluj"))
                .thenReturn(List.of(accommodation));

        List<AccommodationResponse> result =
                service.findByCity("Cluj");

        assertEquals(1, result.size());
        assertEquals(accommodationId, result.getFirst().id());
        assertEquals("Hotel Central", result.getFirst().name());
        assertEquals(
                BigDecimal.valueOf(250),
                result.getFirst().pricePerNight()
        );

        verify(repository)
                .findByCityIgnoreCaseOrderByPricePerNightAsc("Cluj");
    }
}