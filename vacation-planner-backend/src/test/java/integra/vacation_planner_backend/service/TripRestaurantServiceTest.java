
package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.TripRestaurant;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.repository.RestaurantRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.repository.TripRestaurantRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripRestaurantServiceTest {

    @Mock
    private TripRestaurantRepository tripRestaurantRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private TripRestaurantService tripRestaurantService;

    @Test
    void addRestaurantToTrip_shouldThrow_whenTripDoesNotExist() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.empty());

        assertThrows(
            TripNotFoundException.class,
            () -> tripRestaurantService.addRestaurantToTrip(
                tripId,
                "alice",
                UUID.randomUUID(),
                LocalDate.of(2026, 6, 12),
                LocalTime.of(19, 0)
            )
        );
    }

    @Test
    void addRestaurantToTrip_shouldThrow_whenUserIsNotOwner() {
        UUID tripId = UUID.randomUUID();
        Trip trip = createTrip("bob");

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripRestaurantService.addRestaurantToTrip(
                tripId,
                "alice",
                UUID.randomUUID(),
                LocalDate.of(2026, 6, 12),
                LocalTime.of(19, 0)
            )
        );

        verify(restaurantRepository, never()).findById(any());
    }

    @Test
    void addRestaurantToTrip_shouldThrow_whenRestaurantDoesNotExist() {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(createTrip("alice")));
        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripRestaurantService.addRestaurantToTrip(
                tripId,
                "alice",
                restaurantId,
                LocalDate.of(2026, 6, 12),
                LocalTime.of(19, 0)
            )
        );
    }

    @Test
    void addRestaurantToTrip_shouldThrow_whenVisitDateIsOutsideTripDates() {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(createTrip("alice")));

        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(
                createRestaurant(
                    restaurantId,
                    LocalTime.of(10, 0),
                    LocalTime.of(22, 0)
                )
            ));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripRestaurantService.addRestaurantToTrip(
                tripId,
                "alice",
                restaurantId,
                LocalDate.of(2026, 6, 20),
                LocalTime.of(19, 0)
            )
        );

        verify(tripRestaurantRepository, never()).save(any());
    }

    @Test
    void addRestaurantToTrip_shouldThrow_whenVisitTimeIsOutsideOpeningHours() {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(createTrip("alice")));
        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(
                createRestaurant(
                    restaurantId,
                    LocalTime.of(10, 0),
                    LocalTime.of(20, 0)
                )
            ));

        assertThrows(
            IllegalArgumentException.class,
            () -> tripRestaurantService.addRestaurantToTrip(
                tripId,
                "alice",
                restaurantId,
                LocalDate.of(2026, 6, 12),
                LocalTime.of(21, 0)
            )
        );

        verify(tripRestaurantRepository, never()).save(any());
    }

    @Test
    void addRestaurantToTrip_shouldAllowVisit_whenRestaurantClosesAfterMidnight() {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        LocalDate visitDate = LocalDate.of(2026, 6, 12);
        LocalTime visitTime = LocalTime.of(23, 0);

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(createTrip("alice")));
        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(
                createRestaurant(
                    restaurantId,
                    LocalTime.of(18, 0),
                    LocalTime.of(2, 0)
                )
            ));
        when(tripRestaurantRepository
            .existsByTrip_IdAndRestaurant_IdAndVisitDateAndVisitTime(
                tripId,
                restaurantId,
                visitDate,
                visitTime
            )).thenReturn(false);
        when(tripRestaurantRepository.save(any(TripRestaurant.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(
            () -> tripRestaurantService.addRestaurantToTrip(
                tripId,
                "alice",
                restaurantId,
                visitDate,
                visitTime
            )
        );

        verify(tripRestaurantRepository).save(any(TripRestaurant.class));
    }

    @Test
    void addRestaurantToTrip_shouldThrow_whenVisitAlreadyExists() {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        LocalDate visitDate = LocalDate.of(2026, 6, 12);
        LocalTime visitTime = LocalTime.of(19, 0);

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(createTrip("alice")));
        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(
                createRestaurant(
                    restaurantId,
                    LocalTime.of(10, 0),
                    LocalTime.of(22, 0)
                )
            ));
        when(tripRestaurantRepository
            .existsByTrip_IdAndRestaurant_IdAndVisitDateAndVisitTime(
                tripId,
                restaurantId,
                visitDate,
                visitTime
            )).thenReturn(true);

        assertThrows(
            ConflictException.class,
            () -> tripRestaurantService.addRestaurantToTrip(
                tripId,
                "alice",
                restaurantId,
                visitDate,
                visitTime
            )
        );

        verify(tripRestaurantRepository, never()).save(any());
    }

    @Test
    void addRestaurantToTrip_shouldSave_whenVisitIsValid() {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        LocalDate visitDate = LocalDate.of(2026, 6, 12);
        LocalTime visitTime = LocalTime.of(19, 0);

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(createTrip("alice")));
        when(restaurantRepository.findById(restaurantId))
            .thenReturn(Optional.of(
                createRestaurant(
                    restaurantId,
                    LocalTime.of(10, 0),
                    LocalTime.of(22, 0)
                )
            ));
        when(tripRestaurantRepository
            .existsByTrip_IdAndRestaurant_IdAndVisitDateAndVisitTime(
                tripId,
                restaurantId,
                visitDate,
                visitTime
            )).thenReturn(false);
        when(tripRestaurantRepository.save(any(TripRestaurant.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(
            () -> tripRestaurantService.addRestaurantToTrip(
                tripId,
                "alice",
                restaurantId,
                visitDate,
                visitTime
            )
        );

        verify(tripRestaurantRepository).save(any(TripRestaurant.class));
    }

    @Test
    void getRestaurantsForTrip_shouldReturnVisits_whenUserIsOwner() {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        LocalDate visitDate = LocalDate.of(2026, 6, 12);
        LocalTime visitTime = LocalTime.of(19, 0);

        Trip trip = createTrip("alice");
        Restaurant restaurant = createRestaurant(
            restaurantId,
            LocalTime.of(10, 0),
            LocalTime.of(22, 0)
        );
        TripRestaurant visit = new TripRestaurant(
            trip,
            restaurant,
            visitDate,
            visitTime
        );

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(tripRestaurantRepository
            .findByTrip_IdOrderByVisitDateAscVisitTimeAsc(tripId))
            .thenReturn(List.of(visit));

        List<TripRestaurantResponseDTO> result =
            tripRestaurantService.getRestaurantsForTrip(tripId, "alice");

        assertEquals(1, result.size());
        assertEquals(restaurant.getName(), result.get(0).restaurantName());
        assertEquals(restaurantId, result.get(0).restaurantId());
        assertEquals(visitDate, result.get(0).visitDate());
        assertEquals(visitTime, result.get(0).visitTime());
    }

    @Test
    void getRestaurantsForTrip_shouldThrow_whenUserIsNotOwner() {
        UUID tripId = UUID.randomUUID();

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(createTrip("bob")));

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripRestaurantService.getRestaurantsForTrip(
                tripId,
                "alice"
            )
        );

        verify(tripRestaurantRepository, never())
            .findByTrip_IdOrderByVisitDateAscVisitTimeAsc(tripId);
    }

    @Test
    void removeRestaurantFromTrip_shouldDeleteVisit_whenVisitBelongsToTrip() {
        UUID tripId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();

        Trip trip = createTrip("alice");
        trip.setId(tripId);

        TripRestaurant visit = new TripRestaurant(
            trip,
            createRestaurant(
                UUID.randomUUID(),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0)
            ),
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(tripRestaurantRepository.findById(visitId))
            .thenReturn(Optional.of(visit));

        tripRestaurantService.removeRestaurantFromTrip(
            tripId,
            visitId,
            "alice"
        );

        verify(tripRestaurantRepository).delete(visit);
    }

    @Test
    void removeRestaurantFromTrip_shouldThrow_whenVisitDoesNotExist() {
        UUID tripId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();

        Trip trip = createTrip("alice");
        trip.setId(tripId);

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(tripRestaurantRepository.findById(visitId))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> tripRestaurantService.removeRestaurantFromTrip(
                tripId,
                visitId,
                "alice"
            )
        );

        verify(tripRestaurantRepository, never()).delete(any());
    }

    @Test
    void removeRestaurantFromTrip_shouldThrow_whenVisitBelongsToAnotherTrip() {
        UUID tripId = UUID.randomUUID();
        UUID otherTripId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();

        Trip trip = createTrip("alice");
        trip.setId(tripId);

        Trip otherTrip = createTrip("alice");
        otherTrip.setId(otherTripId);

        TripRestaurant visit = new TripRestaurant(
            otherTrip,
            createRestaurant(
                UUID.randomUUID(),
                LocalTime.of(10, 0),
                LocalTime.of(22, 0)
            ),
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));
        when(tripRestaurantRepository.findById(visitId))
            .thenReturn(Optional.of(visit));

        assertThrows(
            NotFoundException.class,
            () -> tripRestaurantService.removeRestaurantFromTrip(
                tripId,
                visitId,
                "alice"
            )
        );

        verify(tripRestaurantRepository, never()).delete(visit);
    }

    @Test
    void removeRestaurantFromTrip_shouldThrow_whenUserIsNotOwner() {
        UUID tripId = UUID.randomUUID();

        Trip trip = createTrip("bob");
        trip.setId(tripId);

        when(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip));

        assertThrows(
            UnauthorizedTripAccessException.class,
            () -> tripRestaurantService.removeRestaurantFromTrip(
                tripId,
                UUID.randomUUID(),
                "alice"
            )
        );

        verify(tripRestaurantRepository, never()).findById(any());
    }

    private Trip createTrip(String username) {
        Trip trip = new Trip();

        User user = new User();
        user.setUsername(username);

        trip.setUser(user);

        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        return trip;
    }

    private Restaurant createRestaurant(
        UUID restaurantId,
        LocalTime openingHour,
        LocalTime closingHour
    ) {
        return new Restaurant(
            restaurantId,
            "Test Restaurant",
            "Main Street",
            openingHour,
            closingHour
        );
    }
}
