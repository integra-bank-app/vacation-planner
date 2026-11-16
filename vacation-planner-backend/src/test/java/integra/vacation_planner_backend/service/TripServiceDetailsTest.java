package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.TripDetailsResponse;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import integra.vacation_planner_backend.repository.FlightRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripServiceDetailsTest {

    private final UUID tripId = UUID.randomUUID();

    private TripRepository tripRepository;

    private TripService tripService;

    private Trip trip;

    @BeforeEach
    void setUp() {
        tripRepository = mock(TripRepository.class);
        tripService = new TripService(
            tripRepository,
            mock(AccommodationRepository.class),
            mock(UserRepository.class),
            mock(FlightRepository.class)
        );

        User owner = new User();
        owner.setUsername("ana.pop");
        owner.setFirstName("Ana");
        owner.setLastName("Pop");

        trip = new Trip();
        trip.setId(tripId);
        trip.setDestination("Paris");
        trip.setStartDate(LocalDate.of(2026, 7, 1));
        trip.setEndDate(LocalDate.of(2026, 7, 4));
        trip.setUser(owner);
    }

    @Test
    void unknownTrip_throwsTripNotFound() {
        when(tripRepository.findWithOwnerById(tripId)).thenReturn(Optional.empty());

        assertThrows(TripNotFoundException.class, () -> tripService.getTripDetails(tripId, "ana.pop"));
    }

    @Test
    void notTheOwner_throwsUnauthorized_andLoadsNothingElse() {
        when(tripRepository.findWithOwnerById(tripId)).thenReturn(Optional.of(trip));

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripService.getTripDetails(tripId, "someone.else")
        );
        verify(tripRepository, never()).findFlightsOfTrip(any(UUID.class));
    }

    @Test
    void blankUsername_isABadRequest() {
        when(tripRepository.findWithOwnerById(tripId)).thenReturn(Optional.of(trip));

        assertThrows(IllegalArgumentException.class, () -> tripService.getTripDetails(tripId, " "));
    }

    @Test
    void owner_getsTheAssembledResponse() {
        Accommodation hotel = new Accommodation(
            UUID.randomUUID(), "Paris Stay", new BigDecimal("150.00"), "Rue de Paris 22", "Paris"
        );
        when(tripRepository.findWithOwnerById(tripId)).thenReturn(Optional.of(trip));
        when(tripRepository.findAccommodationsOfTrip(tripId)).thenReturn(List.of(hotel));
        when(tripRepository.findFlightsOfTrip(tripId)).thenReturn(List.of());
        when(tripRepository.findVisitsOfTrip(tripId)).thenReturn(List.of());

        TripDetailsResponse response = tripService.getTripDetails(tripId, "ana.pop");

        assertEquals("Paris", response.destination());
        assertEquals("ana.pop", response.owner().username());
        assertEquals(1, response.accommodations().size());
        assertEquals(4, response.timeline().size());
        assertEquals(0, response.flights().size());
    }
}
