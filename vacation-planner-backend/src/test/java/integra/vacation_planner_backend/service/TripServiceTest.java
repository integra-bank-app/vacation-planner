package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.TripAccommodationRequest;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.repository.FlightRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.dto.TripResponse;
import integra.vacation_planner_backend.exception.ForbiddenException;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.exception.UserNotFoundException;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import integra.vacation_planner_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    private AccommodationRepository accommodationRepository;

    private UserRepository userRepository;

    private TripService tripService;

    private User bob;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        flightRepository = mock(FlightRepository.class);
        accommodationRepository = mock(AccommodationRepository.class);
        userRepository = mock(UserRepository.class);

        tripService = new TripService(
            tripRepository,
            accommodationRepository,
            userRepository,
            flightRepository
        );

        bob = new User();
        bob.setId(UUID.randomUUID());
        bob.setUsername("bob");
    }

    @Test
    void createTrip_shouldCreateTrip() {
        TripDTO dto = validTripRequest();
        UUID tripId = UUID.randomUUID();
        Trip savedTrip = new Trip();
        savedTrip.setId(tripId);

        when(userRepository.findByUsername("bob"))
            .thenReturn(Optional.of(bob));
        when(tripRepository.save(any(Trip.class)))
            .thenReturn(savedTrip);

        UUID result = tripService.createTrip(dto);

        assertEquals(tripId, result);

        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).save(captor.capture());

        Trip createdTrip = captor.getValue();
        assertSame(bob, createdTrip.getUser());
        assertEquals(dto.getDestination(), createdTrip.getDestination());
        assertEquals(dto.getStartDate(), createdTrip.getStartDate());
        assertEquals(dto.getEndDate(), createdTrip.getEndDate());
    }

    @Test
    void createTrip_shouldRejectMissingUsername() {
        TripDTO dto = validTripRequest();
        dto.setUsername(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectBlankUsername() {
        TripDTO dto = validTripRequest();
        dto.setUsername("   ");

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectMissingDestination() {
        TripDTO dto = validTripRequest();
        dto.setDestination(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectBlankDestination() {
        TripDTO dto = validTripRequest();
        dto.setDestination("   ");

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectMissingStartDate() {
        TripDTO dto = validTripRequest();
        dto.setStartDate(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectMissingEndDate() {
        TripDTO dto = validTripRequest();
        dto.setEndDate(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectInvalidDates() {
        TripDTO dto = validTripRequest();
        dto.setStartDate(LocalDate.of(2026, 6, 20));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_shouldRejectUnknownUser() {
        TripDTO dto = validTripRequest();

        when(userRepository.findByUsername("bob"))
            .thenReturn(Optional.empty());

        assertThrows(
            UserNotFoundException.class,
            () -> tripService.createTrip(dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void getTripsBetweenDates_shouldReturnTrips() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        Trip trip = ownedTrip();

        when(userRepository.findByUsername("bob"))
            .thenReturn(Optional.of(bob));
        when(tripRepository.findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            bob, from, to
        )).thenReturn(List.of(trip));

        List<TripResponse> result =
            tripService.getTripsBetweenDates("bob", from, to);

        assertEquals(
            List.of(new TripResponse(
                trip.getId(),
                "bob",
                "Paris",
                trip.getStartDate(),
                trip.getEndDate()
            )),
            result
        );

        verify(tripRepository)
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                bob, from, to
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
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                any(User.class), any(LocalDate.class), any(LocalDate.class)
            );
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
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                any(User.class), any(LocalDate.class), any(LocalDate.class)
            );
    }

    @Test
    void getTripsBetweenDates_shouldRejectUnknownUser() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        when(userRepository.findByUsername("unknown"))
            .thenReturn(Optional.empty());

        assertThrows(
            UserNotFoundException.class,
            () -> tripService.getTripsBetweenDates("unknown", from, to)
        );

        verify(tripRepository, never())
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                any(User.class), any(LocalDate.class), any(LocalDate.class)
            );
    }

    @Test
    void getTripsBetweenDates_shouldReturnEmptyListWhenNoTripsFound() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        when(userRepository.findByUsername("bob"))
            .thenReturn(Optional.of(bob));
        when(tripRepository.findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            bob, from, to
        )).thenReturn(List.of());

        List<TripResponse> result =
            tripService.getTripsBetweenDates("bob", from, to);

        assertTrue(result.isEmpty());

        verify(tripRepository)
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                bob, from, to
            );
    }

    @Test
    void updateTrip_shouldUpdateStartDate() {
        Trip trip = storedTrip();
        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        tripService.updateTrip(trip.getId(), dto);

        assertEquals(LocalDate.of(2026, 6, 12), trip.getStartDate());
        assertEquals(LocalDate.of(2026, 6, 15), trip.getEndDate());
        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_shouldUpdateEndDate() {
        Trip trip = storedTrip();
        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setEndDate(LocalDate.of(2026, 6, 20));

        tripService.updateTrip(trip.getId(), dto);

        assertEquals(LocalDate.of(2026, 6, 10), trip.getStartDate());
        assertEquals(LocalDate.of(2026, 6, 20), trip.getEndDate());
        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_shouldUpdateBothDates() {
        Trip trip = storedTrip();
        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 7, 1));
        dto.setEndDate(LocalDate.of(2026, 7, 10));

        tripService.updateTrip(trip.getId(), dto);

        assertEquals(LocalDate.of(2026, 7, 1), trip.getStartDate());
        assertEquals(LocalDate.of(2026, 7, 10), trip.getEndDate());
        verify(tripRepository).save(trip);
    }

    @Test
    void updateTrip_shouldRejectMissingUsername() {
        Trip trip = storedTrip();
        TripDTO dto = new TripDTO();
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.updateTrip(trip.getId(), dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectWrongUsername() {
        Trip trip = storedTrip();
        TripDTO dto = new TripDTO();
        dto.setUsername("alice");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripService.updateTrip(trip.getId(), dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectNoDate() {
        Trip trip = storedTrip();
        TripDTO dto = new TripDTO();
        dto.setUsername("bob");

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.updateTrip(trip.getId(), dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectInvalidDates() {
        Trip trip = storedTrip();
        TripDTO dto = validTripRequest();
        dto.setStartDate(LocalDate.of(2026, 6, 20));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.updateTrip(trip.getId(), dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void updateTrip_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();
        TripDTO dto = validTripRequest();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.empty());

        assertThrows(
            TripNotFoundException.class,
            () -> tripService.updateTrip(tripId, dto)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldDeleteTrip() {
        Trip trip = storedTrip();

        tripService.deleteTrip(trip.getId(), "bob");

        verify(tripRepository).delete(trip);
    }

    @Test
    void deleteTrip_shouldRejectMissingUsername() {
        Trip trip = storedTrip();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteTrip(trip.getId(), null)
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldRejectBlankUsername() {
        Trip trip = storedTrip();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteTrip(trip.getId(), "   ")
        );

        verify(tripRepository, never()).delete(any(Trip.class));
    }

    @Test
    void deleteTrip_shouldRejectWrongUsername() {
        Trip trip = storedTrip();

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripService.deleteTrip(trip.getId(), "alice")
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
        Trip trip = storedTrip();
        Accommodation accommodation = storedAccommodation();
        TripAccommodationRequest request =
            new TripAccommodationRequest("bob", accommodation.getId());

        tripService.addAccommodationToTrip(trip.getId(), request);

        assertTrue(trip.getAccommodations().contains(accommodation));
        verify(tripRepository).save(trip);
    }

    @Test
    void addAccommodationToTrip_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();
        TripAccommodationRequest request =
            new TripAccommodationRequest("bob", UUID.randomUUID());

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
        Trip trip = storedTrip();
        UUID accommodationId = UUID.randomUUID();
        TripAccommodationRequest request =
            new TripAccommodationRequest("bob", accommodationId);

        when(accommodationRepository.findById(accommodationId))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.addAccommodationToTrip(trip.getId(), request)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addAccommodationToTrip_shouldRejectWrongUsername() {
        Trip trip = storedTrip();
        Accommodation accommodation = storedAccommodation();
        TripAccommodationRequest request =
            new TripAccommodationRequest("alice", accommodation.getId());

        assertThrows(
            ForbiddenException.class,
            () -> tripService.addAccommodationToTrip(trip.getId(), request)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addAccommodationToTrip_shouldRejectDuplicateAccommodation() {
        Trip trip = storedTrip();
        Accommodation accommodation = storedAccommodation();
        trip.getAccommodations().add(accommodation);

        TripAccommodationRequest request =
            new TripAccommodationRequest("bob", accommodation.getId());

        assertThrows(
            ConflictException.class,
            () -> tripService.addAccommodationToTrip(trip.getId(), request)
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void getTripAccommodations_shouldReturnAccommodationsSortedByPrice() {
        Trip trip = storedTrip();
        Accommodation expensive = accommodation("Expensive Hotel", 500);
        Accommodation cheap = accommodation("Cheap Hotel", 200);
        trip.getAccommodations().add(expensive);
        trip.getAccommodations().add(cheap);

        List<TripAccommodationResponse> result =
            tripService.getTripAccommodations(trip.getId(), "bob");

        assertEquals(
            List.of(
                new TripAccommodationResponse(
                    cheap.getId(),
                    cheap.getName(),
                    cheap.getAddress(),
                    cheap.getCity(),
                    cheap.getPricePerNight()
                ),
                new TripAccommodationResponse(
                    expensive.getId(),
                    expensive.getName(),
                    expensive.getAddress(),
                    expensive.getCity(),
                    expensive.getPricePerNight()
                )
            ),
            result
        );
    }

    @Test
    void getTripAccommodations_shouldRejectWrongUsername() {
        Trip trip = storedTrip();

        assertThrows(
            ForbiddenException.class,
            () -> tripService.getTripAccommodations(trip.getId(), "alice")
        );
    }

    @Test
    void getTripAccommodations_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.getTripAccommodations(tripId, "bob")
        );
    }

    @Test
    void removeAccommodationFromTrip_shouldRemoveAccommodation() {
        Trip trip = storedTrip();
        Accommodation accommodation = storedAccommodation();
        trip.getAccommodations().add(accommodation);

        tripService.removeAccommodationFromTrip(
            trip.getId(), accommodation.getId(), "bob"
        );

        assertTrue(trip.getAccommodations().isEmpty());
        verify(tripRepository).save(trip);
        verify(accommodationRepository, never()).delete(any(Accommodation.class));
    }

    @Test
    void removeAccommodationFromTrip_shouldRejectAccommodationNotInTrip() {
        Trip trip = storedTrip();
        Accommodation accommodation = storedAccommodation();

        assertThrows(
            NotFoundException.class,
            () -> tripService.removeAccommodationFromTrip(
                trip.getId(), accommodation.getId(), "bob"
            )
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void removeAccommodationFromTrip_shouldRejectWrongUsername() {
        Trip trip = storedTrip();
        UUID accommodationId = UUID.randomUUID();

        assertThrows(
            ForbiddenException.class,
            () -> tripService.removeAccommodationFromTrip(
                trip.getId(), accommodationId, "alice"
            )
        );

        verify(tripRepository, never()).save(any(Trip.class));
        verify(accommodationRepository, never()).delete(any(Accommodation.class));
    }

    @Test
    void removeAccommodationFromTrip_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.removeAccommodationFromTrip(
                tripId, accommodationId, "bob"
            )
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void removeAccommodationFromTrip_shouldRejectMissingAccommodation() {
        Trip trip = storedTrip();
        UUID accommodationId = UUID.randomUUID();

        when(accommodationRepository.findById(accommodationId))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.removeAccommodationFromTrip(
                trip.getId(), accommodationId, "bob"
            )
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldAddFlightWhenFlightHasSeats() {
        Trip trip = storedTripWithFlights();
        Flight flight = flight("RO101", 3);

        when(flightRepository.findByFlightNumberForUpdate("RO101"))
            .thenReturn(Optional.of(flight));
        when(tripRepository.countTripsUsingFlight(flight.getId()))
            .thenReturn(1L);

        tripService.addFlightToTrip(trip.getId(), "bob", "RO101");

        assertTrue(trip.getFlights().contains(flight));
        verify(tripRepository).save(trip);
    }

    @Test
    void addFlightToTrip_shouldRejectDuplicateFlight() {
        Flight flight = flight("RO101", 3);
        Trip trip = storedTripWithFlights(flight);

        when(flightRepository.findByFlightNumberForUpdate("RO101"))
            .thenReturn(Optional.of(flight));

        assertThrows(
            ConflictException.class,
            () -> tripService.addFlightToTrip(trip.getId(), "bob", "RO101")
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectFullFlight() {
        Trip trip = storedTripWithFlights();
        Flight flight = flight("RO101", 2);

        when(flightRepository.findByFlightNumberForUpdate("RO101"))
            .thenReturn(Optional.of(flight));
        when(tripRepository.countTripsUsingFlight(flight.getId()))
            .thenReturn(2L);

        assertThrows(
            ConflictException.class,
            () -> tripService.addFlightToTrip(trip.getId(), "bob", "RO101")
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectWrongUsername() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripService.addFlightToTrip(trip.getId(), "alice", "RO101")
        );

        verify(flightRepository, never())
            .findByFlightNumberForUpdate(anyString());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.empty());

        assertThrows(
            TripNotFoundException.class,
            () -> tripService.addFlightToTrip(tripId, "bob", "RO101")
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectMissingFlight() {
        Trip trip = storedTripWithFlights();

        when(flightRepository.findByFlightNumberForUpdate("RO101"))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.addFlightToTrip(trip.getId(), "bob", "RO101")
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectBlankUsername() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.addFlightToTrip(trip.getId(), " ", "RO101")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void addFlightToTrip_shouldRejectBlankFlightNumber() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.addFlightToTrip(trip.getId(), "bob", " ")
        );

        verify(flightRepository, never())
            .findByFlightNumberForUpdate(anyString());
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void getTripFlights_shouldReturnFlightsSortedByDepartureTime() {
        Flight later = flight(
            "RO202", "LHR", "2026-11-10T14:00:00Z", "2026-11-10T16:00:00Z"
        );
        Flight earlier = flight(
            "RO101", "CDG", "2026-11-10T09:00:00Z", "2026-11-10T11:00:00Z"
        );
        Trip trip = storedTripWithFlights(later, earlier);

        List<FlightResponse> result =
            tripService.getTripFlights(trip.getId(), "bob");

        assertEquals(2, result.size());
        assertEquals("RO101", result.get(0).flightNumber());
        assertEquals("RO202", result.get(1).flightNumber());

        assertEquals("OTP", result.get(0).departureAirport());
        assertEquals("CDG", result.get(0).arrivalAirport());
        assertEquals(
            earlier.getDepartureTime().atZone(ZoneId.systemDefault()),
            result.get(0).departureTime()
        );
        assertEquals(
            earlier.getArrivalTime().atZone(ZoneId.systemDefault()),
            result.get(0).arrivalTime()
        );
    }

    @Test
    void getTripFlights_shouldReturnEmptyListWhenTripHasNoFlights() {
        Trip trip = storedTripWithFlights();

        List<FlightResponse> result =
            tripService.getTripFlights(trip.getId(), "bob");

        assertTrue(result.isEmpty());
    }

    @Test
    void getTripFlights_shouldRejectWrongUsername() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripService.getTripFlights(trip.getId(), "alice")
        );
    }

    @Test
    void getTripFlights_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.empty());

        assertThrows(
            TripNotFoundException.class,
            () -> tripService.getTripFlights(tripId, "bob")
        );
    }

    @Test
    void getTripFlights_shouldRejectBlankUsername() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.getTripFlights(trip.getId(), " ")
        );
    }

    @Test
    void getTripFlights_shouldRejectNullUsername() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.getTripFlights(trip.getId(), null)
        );
    }

    @Test
    void deleteFlightFromTrip_shouldRemoveFlightAndKeepOtherFlights() {
        Flight flightToRemove = flight("RO101", 3);
        Flight flightToKeep = flight("RO202", 3);
        Trip trip = storedTripWithFlights(flightToRemove, flightToKeep);

        when(flightRepository.findByFlightNumber("RO101"))
            .thenReturn(Optional.of(flightToRemove));

        tripService.deleteFlightFromTrip(trip.getId(), "RO101", "bob");

        assertEquals(1, trip.getFlights().size());
        assertTrue(trip.getFlights().contains(flightToKeep));
        verify(tripRepository).save(trip);
        verify(flightRepository, never()).delete(any(Flight.class));
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
        Trip trip = storedTripWithFlights();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteFlightFromTrip(trip.getId(), "RO101", null)
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectBlankUsername() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteFlightFromTrip(trip.getId(), "RO101", "   ")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectWrongUsername() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripService.deleteFlightFromTrip(trip.getId(), "RO101", "alice")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectNullFlightNumber() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteFlightFromTrip(trip.getId(), null, "bob")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectBlankFlightNumber() {
        Trip trip = storedTripWithFlights();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.deleteFlightFromTrip(trip.getId(), "   ", "bob")
        );

        verifyNoInteractions(flightRepository);
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectMissingFlight() {
        Trip trip = storedTripWithFlights();

        when(flightRepository.findByFlightNumber("RO101"))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.deleteFlightFromTrip(trip.getId(), "RO101", "bob")
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void deleteFlightFromTrip_shouldRejectFlightNotAssociatedWithTrip() {
        Trip trip = storedTripWithFlights();
        Flight flight = flight("RO101", 3);

        when(flightRepository.findByFlightNumber("RO101"))
            .thenReturn(Optional.of(flight));

        assertThrows(
            NotFoundException.class,
            () -> tripService.deleteFlightFromTrip(trip.getId(), "RO101", "bob")
        );

        verify(tripRepository, never()).save(any(Trip.class));
    }

    private TripDTO validTripRequest() {
        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));
        return dto;
    }

    private Trip ownedTrip() {
        Trip trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setUser(bob);
        trip.setDestination("Paris");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));
        return trip;
    }

    private Trip storedTrip() {
        Trip trip = ownedTrip();
        when(tripRepository.findById(trip.getId()))
            .thenReturn(Optional.of(trip));
        return trip;
    }

    private Accommodation accommodation(String name, int price) {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(UUID.randomUUID());
        accommodation.setName(name);
        accommodation.setAddress("Str. Memorandumului 10");
        accommodation.setCity("Cluj");
        accommodation.setPricePerNight(BigDecimal.valueOf(price));
        return accommodation;
    }

    private Accommodation storedAccommodation() {
        Accommodation accommodation = accommodation("Hotel Central", 250);
        when(accommodationRepository.findById(accommodation.getId()))
            .thenReturn(Optional.of(accommodation));
        return accommodation;
    }

    private Trip storedTripWithFlights(Flight... flights) {
        Trip trip = storedTrip();
        trip.setFlights(new HashSet<>(List.of(flights)));
        return trip;
    }

    private Flight flight(String flightNumber, int numberOfSeats) {
        Flight flight = new Flight();
        flight.setId(UUID.randomUUID());
        flight.setFlightNumber(flightNumber);
        flight.setNumberOfSeats(numberOfSeats);
        return flight;
    }

    private Flight flight(
        String flightNumber,
        String arrivalAirportCode,
        String departureTime,
        String arrivalTime
    ) {
        Flight flight = flight(flightNumber, 3);
        flight.setDepartureAirportCode("OTP");
        flight.setArrivalAirportCode(arrivalAirportCode);
        flight.setDepartureTime(Instant.parse(departureTime));
        flight.setArrivalTime(Instant.parse(arrivalTime));
        return flight;
    }
}
