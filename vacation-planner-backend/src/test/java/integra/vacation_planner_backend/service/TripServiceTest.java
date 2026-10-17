package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.TripAccommodationRequest;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.dto.TripResponse;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.ForbiddenException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.exception.UserNotFoundException;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import integra.vacation_planner_backend.dto.PageResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import static org.mockito.Mockito.when;

class TripServiceTest {

    private TripRepository tripRepository;

    private AccommodationRepository accommodationRepository;

    private UserRepository userRepository;

    private TripService tripService;

    private User bob;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        accommodationRepository = mock(AccommodationRepository.class);
        userRepository = mock(UserRepository.class);

        tripService = new TripService(
            tripRepository,
            accommodationRepository,
            userRepository
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

        PageRequest pageable = PageRequest.of(
            1,
            10,
            Sort.by(
                Sort.Order.asc("startDate"),
                Sort.Order.asc("id")
            )
        );

        when(userRepository.findByUsername("bob"))
            .thenReturn(Optional.of(bob));
        when(tripRepository.findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            bob, from, to, pageable
        )).thenReturn(
            new PageImpl<>(List.of(trip), pageable, 25)
        );

        PageResponse<TripResponse> result =
            tripService.getTripsBetweenDates("bob", from, to, 1, 10);

        assertEquals(
            List.of(new TripResponse(
                trip.getId(),
                "bob",
                "Paris",
                trip.getStartDate(),
                trip.getEndDate()
            )),
            result.content()
        );
        assertEquals(1, result.page());
        assertEquals(10, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());

        verify(tripRepository)
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                bob, from, to, pageable
            );
    }

    @Test
    void getTripsBetweenDates_shouldRejectBlankUsername() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.getTripsBetweenDates("   ", from, to, 0, 10)
        );

        verify(tripRepository, never())
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                any(User.class),
                any(LocalDate.class),
                any(LocalDate.class),
                any(Pageable.class)
            );
    }

    @Test
    void getTripsBetweenDates_shouldRejectInvalidDateRange() {
        LocalDate from = LocalDate.of(2026, 6, 30);
        LocalDate to = LocalDate.of(2026, 6, 1);

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.getTripsBetweenDates("bob", from, to, 0, 10)
        );

        verify(tripRepository, never())
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                any(User.class),
                any(LocalDate.class),
                any(LocalDate.class),
                any(Pageable.class)
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
            () -> tripService.getTripsBetweenDates("unknown", from, to, 0, 10)
        );

        verify(tripRepository, never())
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                any(User.class),
                any(LocalDate.class),
                any(LocalDate.class),
                any(Pageable.class)
            );
    }

    @Test
    void getTripsBetweenDates_shouldReturnEmptyPageWhenNoTripsFound() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        PageRequest pageable = PageRequest.of(
            0,
            10,
            Sort.by(
                Sort.Order.asc("startDate"),
                Sort.Order.asc("id")
            )
        );

        when(userRepository.findByUsername("bob"))
            .thenReturn(Optional.of(bob));
        when(tripRepository.findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            bob, from, to, pageable
        )).thenReturn(
            new PageImpl<Trip>(List.of(), pageable, 0)
        );

        PageResponse<TripResponse> result =
            tripService.getTripsBetweenDates("bob", from, to, 0, 10);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(0L, result.totalElements());
        assertEquals(0, result.totalPages());

        verify(tripRepository)
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                bob, from, to, pageable
            );
    }

    @Test
    void getTripsBetweenDates_pageBeyondEnd_keepsTotals() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        PageRequest pageable = PageRequest.of(
            3,
            10,
            Sort.by(
                Sort.Order.asc("startDate"),
                Sort.Order.asc("id")
            )
        );

        when(userRepository.findByUsername("bob"))
            .thenReturn(Optional.of(bob));
        when(tripRepository.findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            bob, from, to, pageable
        )).thenReturn(
            new PageImpl<Trip>(List.of(), pageable, 25)
        );

        PageResponse<TripResponse> result =
            tripService.getTripsBetweenDates("bob", from, to, 3, 10);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.page());
        assertEquals(10, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());
    }

    @Test
    void getTripsBetweenDates_shouldRejectInvalidPagination() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.getTripsBetweenDates("bob", from, to, -1, 10)
        );

        for (int size : List.of(-1, 0, 1, 7, 15, 101)) {
            assertThrows(
                IllegalArgumentException.class,
                () -> tripService.getTripsBetweenDates("bob", from, to, 0, size)
            );
        }

        verify(tripRepository, never())
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                any(User.class),
                any(LocalDate.class),
                any(LocalDate.class),
                any(Pageable.class)
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
    void getTripAccommodations_shouldUseDatabasePaginationAndNameSort() {
        Trip trip = storedTrip();
        Accommodation accommodation = accommodation("Alpha Hotel", 500);

        PageRequest pageable = PageRequest.of(
            1,
            5,
            Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
            )
        );

        when(tripRepository.findAccommodationsByTripId(trip.getId(), pageable))
            .thenReturn(new PageImpl<>(
                List.of(accommodation),
                pageable,
                6
            ));

        PageResponse<TripAccommodationResponse> result =
            tripService.getTripAccommodations(trip.getId(), "bob", 1, 5);

        assertEquals(
            List.of(new TripAccommodationResponse(
                accommodation.getId(),
                accommodation.getName(),
                accommodation.getAddress(),
                accommodation.getCity(),
                accommodation.getPricePerNight()
            )),
            result.content()
        );
        assertEquals(1, result.page());
        assertEquals(5, result.size());
        assertEquals(6L, result.totalElements());
        assertEquals(2, result.totalPages());

        verify(tripRepository)
            .findAccommodationsByTripId(trip.getId(), pageable);
    }

    @Test
    void getTripAccommodations_shouldRejectWrongUsername() {
        Trip trip = storedTrip();

        assertThrows(
            ForbiddenException.class,
            () -> tripService.getTripAccommodations(trip.getId(), "alice", 0, 10)
        );

        verify(tripRepository, never())
            .findAccommodationsByTripId(any(UUID.class), any(Pageable.class));
    }

    @Test
    void getTripAccommodations_shouldRejectMissingTrip() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripService.getTripAccommodations(tripId, "bob", 0, 10)
        );

        verify(tripRepository, never())
            .findAccommodationsByTripId(any(UUID.class), any(Pageable.class));
    }

    @Test
    void getTripAccommodations_pageBeyondEnd_keepsTotals() {
        Trip trip = storedTrip();

        PageRequest pageable = PageRequest.of(
            3,
            10,
            Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
            )
        );

        when(tripRepository.findAccommodationsByTripId(trip.getId(), pageable))
            .thenReturn(new PageImpl<Accommodation>(
                List.of(),
                pageable,
                25
            ));

        PageResponse<TripAccommodationResponse> result =
            tripService.getTripAccommodations(trip.getId(), "bob", 3, 10);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.page());
        assertEquals(10, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());

        verify(tripRepository)
            .findAccommodationsByTripId(trip.getId(), pageable);
    }

    @Test
    void getTripAccommodations_shouldRejectInvalidPagination() {
        UUID tripId = UUID.randomUUID();

        assertThrows(
            IllegalArgumentException.class,
            () -> tripService.getTripAccommodations(tripId, "bob", -1, 10)
        );

        for (int size : List.of(-1, 0, 1, 7, 15, 101)) {
            assertThrows(
                IllegalArgumentException.class,
                () -> tripService.getTripAccommodations(tripId, "bob", 0, size)
            );
        }

        verify(tripRepository, never())
            .findAccommodationsByTripId(any(UUID.class), any(Pageable.class));
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
}
