package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.repository.FlightRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;

public class TripServiceTest {

    private TripRepository tripRepository;

    private FlightRepository flightRepository;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        flightRepository = mock(FlightRepository.class);
        tripService = new TripService(tripRepository, flightRepository);
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

    // TESTS FOR GET TRIPS

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

    // TESTS FOR UPDATE METHOD

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

    // TESTS FOR THE DELETE METHOD

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

    // TESTS FOR ADDING A FLIGHT TO A TRIP

    @Test
    void addFlightToTrip_shouldAddFlightWhenTripBelongsToUserAndFlightHasSeats() {
        UUID tripId = UUID.randomUUID();
        UUID flightId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setFlights(new HashSet<>());

        Flight flight = new Flight();
        flight.setId(flightId);
        flight.setFlightNumber("RO101");
        flight.setNumberOfSeats(3);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(flightRepository.findByFlightNumberForUpdate("RO101"))
            .thenReturn(Optional.of(flight));
        when(tripRepository.countTripsUsingFlight(flightId)).thenReturn(1L);

        tripService.addFlightToTrip(tripId, "bob", "RO101");

        assertTrue(trip.getFlights().contains(flight));
        verify(tripRepository).save(trip);
    }

    @Test
    void addFlightToTrip_shouldRejectDuplicateFlight() {
        UUID tripId = UUID.randomUUID();
        UUID flightId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        Flight flight = new Flight();
        flight.setId(flightId);
        flight.setFlightNumber("RO101");
        flight.setNumberOfSeats(3);

        trip.setFlights(new HashSet<>(List.of(flight)));

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(flightRepository.findByFlightNumberForUpdate("RO101"))
            .thenReturn(Optional.of(flight));

        assertThrows(ConflictException.class,
            () -> tripService.addFlightToTrip(tripId, "bob", "RO101"));

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectFullFlight() {
        UUID tripId = UUID.randomUUID();
        UUID flightId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setFlights(new HashSet<>());

        Flight flight = new Flight();
        flight.setId(flightId);
        flight.setFlightNumber("RO101");
        flight.setNumberOfSeats(2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(flightRepository.findByFlightNumberForUpdate("RO101"))
            .thenReturn(Optional.of(flight));
        when(tripRepository.countTripsUsingFlight(flightId)).thenReturn(2L);

        assertThrows(ConflictException.class,
            () -> tripService.addFlightToTrip(tripId, "bob", "RO101"));

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectTripOwnedByAnotherUser() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("alice");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        assertThrows(UnauthorizedTripAccessException.class,
            () -> tripService.addFlightToTrip(tripId, "bob", "RO101"));

        verify(flightRepository, never()).findByFlightNumberForUpdate(anyString());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        assertThrows(TripNotFoundException.class,
            () -> tripService.addFlightToTrip(tripId, "bob", "RO101"));

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectMissingFlight() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setFlights(new HashSet<>());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(flightRepository.findByFlightNumberForUpdate("RO101"))
            .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> tripService.addFlightToTrip(tripId, "bob", "RO101"));

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectMissingUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("tudor");
        trip.setFlights(new HashSet<>());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.addFlightToTrip(tripId, " ", "RO101")
        );

        verifyNoInteractions(flightRepository);
    }

    @Test
    void addFlightToTrip_shouldRejectMissingFlightNumber() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("tudor");
        trip.setFlights(new HashSet<>());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.addFlightToTrip(tripId, "tudor", " ")
        );

        verify(flightRepository, never()).findByFlightNumberForUpdate(anyString());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void getTripFlights_shouldReturnFlightsSortedByDepartureTime() {
        UUID tripId = UUID.randomUUID();

        Flight laterFlight = new Flight();
        laterFlight.setId(UUID.randomUUID());
        laterFlight.setFlightNumber("RO202");
        laterFlight.setDepartureAirportCode("OTP");
        laterFlight.setArrivalAirportCode("LHR");
        laterFlight.setDepartureTime(Instant.parse("2026-11-10T14:00:00Z"));
        laterFlight.setArrivalTime(Instant.parse("2026-11-10T16:00:00Z"));

        Flight earlierFlight = new Flight();
        earlierFlight.setId(UUID.randomUUID());
        earlierFlight.setFlightNumber("RO101");
        earlierFlight.setDepartureAirportCode("OTP");
        earlierFlight.setArrivalAirportCode("CDG");
        earlierFlight.setDepartureTime(Instant.parse("2026-11-10T09:00:00Z"));
        earlierFlight.setArrivalTime(Instant.parse("2026-11-10T11:00:00Z"));

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setFlights(new HashSet<>(List.of(laterFlight, earlierFlight)));

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        List<FlightResponse> result = tripService.getTripFlights(tripId, "bob");

        assertEquals(2, result.size());

        // Flights are sorted by departure time
        assertEquals("RO101", result.get(0).flightNumber());
        assertEquals("RO202", result.get(1).flightNumber());

        // DTO fields are mapped correctly
        assertEquals("OTP", result.get(0).departureAirport());
        assertEquals("CDG", result.get(0).arrivalAirport());
        assertEquals(
            earlierFlight.getDepartureTime().atZone(ZoneId.systemDefault()),
            result.get(0).departureTime()
        );
        assertEquals(
            earlierFlight.getArrivalTime().atZone(ZoneId.systemDefault()),
            result.get(0).arrivalTime()
        );
    }

    @Test
    void getTripFlights_shouldReturnEmptyListWhenTripHasNoFlights() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setFlights(new HashSet<>());

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        List<FlightResponse> result = tripService.getTripFlights(tripId, "bob");

        assertTrue(result.isEmpty());
    }

    @Test
    void getTripFlights_shouldRejectWrongUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripService.getTripFlights(tripId, "alice")
        );
    }

    @Test
    void getTripFlights_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        assertThrows(
            TripNotFoundException.class,
            () -> tripService.getTripFlights(tripId, "bob")
        );
    }

    @Test
    void getTripFlights_shouldRejectBlankUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.getTripFlights(tripId, " ")
        );
    }

    @Test
    void getTripFlights_shouldRejectNullUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.getTripFlights(tripId, null)
        );
    }

    // TESTS FOR REMOVING A FLIGHT FROM A TRIP

    @Test
    void deleteFlightFromTrip_shouldRemoveFlightAndKeepOtherFlights() {
        UUID tripId = UUID.randomUUID();
        UUID flightId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        Flight flightToRemove = new Flight();
        flightToRemove.setId(flightId);
        flightToRemove.setFlightNumber("RO101");

        Flight flightToKeep = new Flight();
        flightToKeep.setId(UUID.randomUUID());
        flightToKeep.setFlightNumber("RO202");

        trip.setFlights(new HashSet<>(List.of(flightToRemove, flightToKeep)));

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(flightRepository.findByFlightNumber("RO101"))
            .thenReturn(Optional.of(flightToRemove));

        tripService.deleteFlightFromTrip(tripId, "RO101", "bob");

        assertEquals(1, trip.getFlights().size());
        assertTrue(trip.getFlights().contains(flightToKeep));
        assertTrue(trip.getFlights().stream()
            .noneMatch(f -> f.getId().equals(flightId)));

        verify(tripRepository).save(trip);
        verify(flightRepository, never()).delete(flightToRemove);
    }

    @Test
    void deleteFlightFromTrip_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.empty());

        assertThrows(
            TripNotFoundException.class,
            () -> tripService.deleteFlightFromTrip(tripId, "RO101", "bob")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectNullUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteFlightFromTrip(tripId, "RO101", null)
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectBlankUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteFlightFromTrip(tripId, "RO101", "   ")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectWrongUsername() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("alice");

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripService.deleteFlightFromTrip(tripId, "RO101", "bob")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectNullFlightNumber() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteFlightFromTrip(tripId, null, "bob")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectBlankFlightNumber() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteFlightFromTrip(tripId, "   ", "bob")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectMissingFlight() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setFlights(new HashSet<>());

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(flightRepository.findByFlightNumber("RO101"))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.deleteFlightFromTrip(tripId, "RO101", "bob")
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectFlightNotAssociatedWithTrip() {
        UUID tripId = UUID.randomUUID();

        Trip trip = new Trip();
        trip.setId(tripId);
        trip.setUsername("bob");
        trip.setFlights(new HashSet<>());

        Flight flight = new Flight();
        flight.setId(UUID.randomUUID());
        flight.setFlightNumber("RO101");

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(flightRepository.findByFlightNumber("RO101"))
            .thenReturn(Optional.of(flight));

        assertThrows(
            NotFoundException.class,
            () -> tripService.deleteFlightFromTrip(tripId, "RO101", "bob")
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }
}
