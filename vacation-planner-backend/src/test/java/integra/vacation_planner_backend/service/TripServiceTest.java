package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.dto.TripAccommodationRequest;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.ForbiddenException;
import integra.vacation_planner_backend.exception.NotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

public class TripServiceTest {

    private TripRepository tripRepository;

    private AccommodationRepository accommodationRepository;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        accommodationRepository = mock(AccommodationRepository.class);
        tripService = new TripService(tripRepository, accommodationRepository);
    }


    @Test
    void createTrip_shouldCreateTrip() {
        TripDTO dto = new TripDTO();

        dto.setUsername("bob");
        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        UUID tripId = UUID.randomUUID();

        Trip savedTrip = new Trip();
        savedTrip.setId(tripId);

        when(tripRepository.save(any(Trip.class)))
                .thenReturn(savedTrip);

        UUID result = tripService.createTrip(dto);

        assertEquals(tripId, result);

        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectMissingUsername() {
        TripDTO dto = new TripDTO();

        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectBlankUsername() {
        TripDTO dto = new TripDTO();

        dto.setUsername("   ");
        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectMissingDestination() {
        TripDTO dto = new TripDTO();

        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectBlankDestination() {
        TripDTO dto = new TripDTO();

        dto.setUsername("bob");
        dto.setDestination("   ");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectMissingStartDate() {
        TripDTO dto = new TripDTO();

        dto.setUsername("bob");
        dto.setDestination("Paris");
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectMissingEndDate() {
        TripDTO dto = new TripDTO();

        dto.setUsername("bob");
        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 10));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectInvalidDates() {
        TripDTO dto = new TripDTO();

        dto.setUsername("bob");
        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 20));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void getTripsBetweenDates_shouldReturnTrips() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        Trip trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setUsername("bob");
        trip.setDestination("Paris");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripRepository
                .findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                        "bob", from, to))
                .thenReturn(List.of(trip));

        List<Trip> result =
                tripService.getTripsBetweenDates("bob", from, to);

        assertEquals(1, result.size());
        assertEquals(trip, result.get(0));

        verify(tripRepository)
            .findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                "bob",
                from,
                to
            );
    }

    @Test
    void getTripsBetweenDates_shouldRejectBlankUsername() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.getTripsBetweenDates("   ", from, to)
        );

        verify(tripRepository, never())
                .findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                        anyString(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getTripsBetweenDates_shouldRejectInvalidDateRange() {
        LocalDate from = LocalDate.of(2026, 6, 30);
        LocalDate to = LocalDate.of(2026, 6, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.getTripsBetweenDates("bob", from, to)
        );

        verify(tripRepository, never())
                .findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                        anyString(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getTripsBetweenDates_shouldReturnEmptyListWhenNoTripsFound() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        when(tripRepository
                .findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                        "bob", from, to))
                .thenReturn(List.of());

        List<Trip> result =
                tripService.getTripsBetweenDates("bob", from, to);

        assertTrue(result.isEmpty());

        verify(tripRepository)
            .findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                "bob",
                from,
                to
            );
    }

    @Test
    void updateTrip_shouldUpdateStartDate() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        tripService.updateTrip(tripId, dto);

        assertEquals(LocalDate.of(2026, 6, 12), trip.getStartDate());
        assertEquals(LocalDate.of(2026, 6, 15), trip.getEndDate());

        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_shouldUpdateEndDate() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setEndDate(LocalDate.of(2026, 6, 20));

        tripService.updateTrip(tripId, dto);

        assertEquals(LocalDate.of(2026, 6, 10), trip.getStartDate());
        assertEquals(LocalDate.of(2026, 6, 20), trip.getEndDate());

        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_shouldUpdateBothDates() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 7, 1));
        dto.setEndDate(LocalDate.of(2026, 7, 10));

        tripService.updateTrip(tripId, dto);

        assertEquals(LocalDate.of(2026, 7, 1), trip.getStartDate());
        assertEquals(LocalDate.of(2026, 7, 10), trip.getEndDate());

        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_shouldRejectMissingUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.updateTrip(tripId, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectWrongUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("alice");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        assertThrows(
                UnauthorizedTripAccessException.class,
                () -> tripService.updateTrip(tripId, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectNoDate() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.updateTrip(tripId, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectInvalidDates() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 20));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.updateTrip(tripId, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.empty());

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        assertThrows(
                TripNotFoundException.class,
                () -> tripService.updateTrip(tripId, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldDeleteTrip() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        tripService.deleteTrip(tripId, "bob");

        verify(tripRepository).delete(trip);
    }

    @Test
    void deleteTrip_shouldRejectMissingUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.deleteTrip(tripId, null)
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldRejectBlankUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.deleteTrip(tripId, "   ")
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldRejectWrongUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.of(trip));

        assertThrows(
                UnauthorizedTripAccessException.class,
                () -> tripService.deleteTrip(tripId, "alice")
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
                .thenReturn(Optional.empty());

        assertThrows(
                TripNotFoundException.class,
                () -> tripService.deleteTrip(tripId, "bob")
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void addAccommodationToTrip_shouldAddAccommodation() {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationId);
        accommodation.setName("Hotel Central");
        accommodation.setPricePerNight(BigDecimal.valueOf(250));
        accommodation.setAddress("Str. Memorandumului 10");
        accommodation.setCity("Cluj");

        TripAccommodationRequest request = new TripAccommodationRequest(
            "bob",
            accommodationId
        );

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(accommodationRepository.findById(accommodationId))
            .thenReturn(Optional.of(accommodation));

        tripService.addAccommodationToTrip(tripId, request);

        assertTrue(trip.getAccommodations().contains(accommodation));
        verify(tripRepository).save(trip);
    }

    @Test
    void addAccommodationToTrip_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        TripAccommodationRequest request = new TripAccommodationRequest(
            "bob",
            accommodationId
        );

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.addAccommodationToTrip(tripId, request)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addAccommodationToTrip_shouldRejectMissingAccommodation() {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        TripAccommodationRequest request = new TripAccommodationRequest(
            "bob",
            accommodationId
        );

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(accommodationRepository.findById(accommodationId))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.addAccommodationToTrip(tripId, request)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addAccommodationToTrip_shouldRejectWrongUsername() {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationId);

        TripAccommodationRequest request = new TripAccommodationRequest(
            "alice",
            accommodationId
        );

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(accommodationRepository.findById(accommodationId))
            .thenReturn(Optional.of(accommodation));

        assertThrows(
            ForbiddenException.class,
            () -> tripService.addAccommodationToTrip(tripId, request)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addAccommodationToTrip_shouldRejectDuplicateAccommodation() {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationId);

        trip.getAccommodations().add(accommodation);

        TripAccommodationRequest request = new TripAccommodationRequest(
            "bob",
            accommodationId
        );

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(accommodationRepository.findById(accommodationId))
            .thenReturn(Optional.of(accommodation));

        assertThrows(
            ConflictException.class,
            () -> tripService.addAccommodationToTrip(tripId, request)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void getTripAccommodations_shouldReturnAccommodationsSortedByPrice() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        Accommodation expensive = new Accommodation();
        expensive.setId(UUID.randomUUID());
        expensive.setName("Expensive Hotel");
        expensive.setAddress("Str. A 1");
        expensive.setCity("Cluj");
        expensive.setPricePerNight(BigDecimal.valueOf(500));

        Accommodation cheap = new Accommodation();
        cheap.setId(UUID.randomUUID());
        cheap.setName("Cheap Hotel");
        cheap.setAddress("Str. B 2");
        cheap.setCity("Cluj");
        cheap.setPricePerNight(BigDecimal.valueOf(200));

        trip.getAccommodations().add(expensive);
        trip.getAccommodations().add(cheap);

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));

        List<TripAccommodationResponse> result =
            tripService.getTripAccommodations(tripId, "bob");

        assertEquals(2, result.size());
        assertEquals("Cheap Hotel", result.get(0).name());
        assertEquals(BigDecimal.valueOf(200), result.get(0).pricePerNight());
        assertEquals("Expensive Hotel", result.get(1).name());
        assertEquals(BigDecimal.valueOf(500), result.get(1).pricePerNight());
    }

    @Test
    void getTripAccommodations_shouldRejectWrongUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));

        assertThrows(
            ForbiddenException.class,
            () -> tripService.getTripAccommodations(tripId, "alice")
        );
    }

    @Test
    void removeAccommodationFromTrip_shouldRemoveAccommodation() {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationId);

        trip.getAccommodations().add(accommodation);

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(accommodationRepository.findById(accommodationId))
            .thenReturn(Optional.of(accommodation));

        tripService.removeAccommodationFromTrip(tripId, accommodationId, "bob");

            assertTrue(trip.getAccommodations().isEmpty());
             verify(tripRepository).save(trip);
    }

    @Test
    void removeAccommodationFromTrip_shouldRejectAccommodationNotInTrip() {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationId);

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(accommodationRepository.findById(accommodationId))
            .thenReturn(Optional.of(accommodation));

        assertThrows(
            NotFoundException.class,
            () -> tripService.removeAccommodationFromTrip(
                tripId,
                accommodationId,
                "bob"
            )
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

}
