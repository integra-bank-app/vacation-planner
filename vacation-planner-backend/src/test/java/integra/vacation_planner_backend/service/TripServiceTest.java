package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class TripServiceTest {

    private TripRepository tripRepository;
    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        tripService = new TripService(tripRepository);
    }

/// TESTS FOR CREATE

    @Test
    void createTrip_shouldCreateTrip() {
        TripDTO dto = new TripDTO();

        dto.setUsername("bob");
        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        Trip savedTrip = new Trip();
        savedTrip.setId(1L);

        when(tripRepository.save(any(Trip.class)))
                .thenReturn(savedTrip);

        Long result = tripService.createTrip(dto);

        assertEquals(1L, result);

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


/// TESTS FOR GET TRIPS


    @Test
    void getTripsBetweenDates_shouldReturnTrips() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        Trip trip = new Trip();
        trip.setId(1L);
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
                        "bob", from, to);
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
                        "bob", from, to);
    }



/// TESTS FOR UPDATE METHOD



    @Test
    void updateTrip_shouldUpdateStartDate() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        tripService.updateTrip(1L, dto);

        assertEquals(LocalDate.of(2026, 6, 12), trip.getStartDate());
        assertEquals(LocalDate.of(2026, 6, 15), trip.getEndDate());

        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_shouldUpdateEndDate() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setEndDate(LocalDate.of(2026, 6, 20));

        tripService.updateTrip(1L, dto);

        assertEquals(LocalDate.of(2026, 6, 10), trip.getStartDate());
        assertEquals(LocalDate.of(2026, 6, 20), trip.getEndDate());

        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_shouldUpdateBothDates() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 7, 1));
        dto.setEndDate(LocalDate.of(2026, 7, 10));

        tripService.updateTrip(1L, dto);

        assertEquals(LocalDate.of(2026, 7, 1), trip.getStartDate());
        assertEquals(LocalDate.of(2026, 7, 10), trip.getEndDate());

        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_shouldRejectMissingUsername() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.updateTrip(1L, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectWrongUsername() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("alice");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        assertThrows(
                UnauthorizedTripAccessException.class,
                () -> tripService.updateTrip(1L, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectNoDate() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.updateTrip(1L, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectInvalidDates() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 20));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.updateTrip(1L, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectMissingTrip() {
        when(tripRepository.findById(1L))
                .thenReturn(Optional.empty());

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        assertThrows(
                TripNotFoundException.class,
                () -> tripService.updateTrip(1L, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

/// TESTS FOR THE DELETE METHOD


    @Test
    void deleteTrip_shouldDeleteTrip() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        tripService.deleteTrip(1L, "bob");

        verify(tripRepository).delete(trip);
    }

    @Test
    void deleteTrip_shouldRejectMissingUsername() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.deleteTrip(1L, null)
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldRejectBlankUsername() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.deleteTrip(1L, "   ")
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldRejectWrongUsername() {
        Trip trip = new Trip();
        trip.setId(1L);
        trip.setUsername("bob");

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        assertThrows(
                UnauthorizedTripAccessException.class,
                () -> tripService.deleteTrip(1L, "alice")
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldRejectMissingTrip() {
        when(tripRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                TripNotFoundException.class,
                () -> tripService.deleteTrip(1L, "bob")
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }
}
