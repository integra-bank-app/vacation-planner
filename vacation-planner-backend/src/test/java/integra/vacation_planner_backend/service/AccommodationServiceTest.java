package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.dto.AccommodationRequest;
import integra.vacation_planner_backend.dto.AccommodationResponse;
import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.exception.AccommodationNotFoundException;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import integra.vacation_planner_backend.dto.PageResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

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

        PageRequest pageable = PageRequest.of(
            1,
            10,
            Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
            )
        );

        when(repository.findByCityIgnoreCase("Cluj", pageable))
            .thenReturn(new PageImpl<>(
                List.of(accommodation),
                pageable,
                25
            ));

        PageResponse<AccommodationResponse> result =
            service.findByCity("Cluj", 1, 10);

        assertEquals(
            List.of(new AccommodationResponse(
                accommodationId,
                "Hotel Central",
                BigDecimal.valueOf(250)
            )),
            result.content()
        );
        assertEquals(1, result.page());
        assertEquals(10, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());

        verify(repository).findByCityIgnoreCase("Cluj", pageable);
    }

    @Test
    void findByCity_pageBeyondEnd_returnsEmptyContentAndKeepsTotals() {
        PageRequest pageable = PageRequest.of(
            3,
            10,
            Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
            )
        );

        when(repository.findByCityIgnoreCase("Cluj", pageable))
            .thenReturn(new PageImpl<Accommodation>(
                List.of(),
                pageable,
                25
            ));

        PageResponse<AccommodationResponse> result =
            service.findByCity("Cluj", 3, 10);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.page());
        assertEquals(10, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());

        verify(repository).findByCityIgnoreCase("Cluj", pageable);
    }

    @Test
    void findByCity_negativePage_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.findByCity("Cluj", -1, 10)
        );

        assertEquals(
            "page must be greater than or equal to 0",
            exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void findByCity_invalidSizes_throwIllegalArgumentException() {
        for (int size : List.of(-1, 0, 1, 7, 15, 101)) {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.findByCity("Cluj", 0, size)
            );

            assertEquals(
                "size must be one of: 5, 10, 20, 50, 100",
                exception.getMessage()
            );
        }

        verifyNoInteractions(repository);
    }

    @Test
    void findByCity_allowedSizes_arePassedToRepository() {
        for (int size : List.of(5, 10, 20, 50, 100)) {
            PageRequest pageable = PageRequest.of(
                0,
                size,
                Sort.by(
                    Sort.Order.asc("name"),
                    Sort.Order.asc("id")
                )
            );

            when(repository.findByCityIgnoreCase("Cluj", pageable))
                .thenReturn(new PageImpl<Accommodation>(
                    List.of(),
                    pageable,
                    0
                ));

            PageResponse<AccommodationResponse> result =
                service.findByCity("Cluj", 0, size);

            assertTrue(result.content().isEmpty());
            assertEquals(0, result.page());
            assertEquals(size, result.size());
            assertEquals(0L, result.totalElements());
            assertEquals(0, result.totalPages());

            verify(repository).findByCityIgnoreCase("Cluj", pageable);
        }
    }
}
