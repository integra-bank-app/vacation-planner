package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.TripRestaurant;
import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;
import integra.vacation_planner_backend.repository.RestaurantRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.repository.TripRestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TripRestaurantIntegrationTest {

    @Autowired
    private TripRestaurantService tripRestaurantService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TripRestaurantRepository tripRestaurantRepository;

    private Trip trip;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        trip = new Trip();
        trip.setDestination("Rome");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));
        trip.setUsername("alice");
        trip = tripRepository.saveAndFlush(trip);

        restaurant = new Restaurant(
            UUID.randomUUID(),
            "Test Restaurant",
            "Main Street",
            LocalTime.of(9, 0),
            LocalTime.of(22, 0)
        );
        restaurant = restaurantRepository.saveAndFlush(restaurant);
    }

    @Test
    void addRestaurantToTrip_shouldPersistVisit() {
        UUID visitId = tripRestaurantService.addRestaurantToTrip(
            trip.getId(),
            "alice",
            restaurant.getId(),
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        assertNotNull(visitId);

        TripRestaurant savedVisit = tripRestaurantRepository
            .findById(visitId)
            .orElseThrow();

        assertEquals(trip.getId(), savedVisit.getTrip().getId());
        assertEquals(restaurant.getId(), savedVisit.getRestaurant().getId());
        assertEquals(LocalDate.of(2026, 6, 12), savedVisit.getVisitDate());
        assertEquals(LocalTime.of(19, 0), savedVisit.getVisitTime());
    }

    @Test
    void getRestaurantsForTrip_shouldReturnPersistedVisits() {
        tripRestaurantService.addRestaurantToTrip(
            trip.getId(),
            "alice",
            restaurant.getId(),
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        List<TripRestaurantResponseDTO> visits =
            tripRestaurantService.getRestaurantsForTrip(
                trip.getId(),
                "alice"
            );

        assertEquals(1, visits.size());
        assertEquals(restaurant.getId(), visits.get(0).restaurantId());
        assertEquals("Test Restaurant", visits.get(0).restaurantName());
        assertEquals(LocalDate.of(2026, 6, 12), visits.get(0).visitDate());
        assertEquals(LocalTime.of(19, 0), visits.get(0).visitTime());
    }

    @Test
    void getRestaurantsForTrip_shouldReturnVisitsInChronologicalOrder() {
        tripRestaurantService.addRestaurantToTrip(
            trip.getId(),
            "alice",
            restaurant.getId(),
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        tripRestaurantService.addRestaurantToTrip(
            trip.getId(),
            "alice",
            restaurant.getId(),
            LocalDate.of(2026, 6, 11),
            LocalTime.of(18, 0)
        );

        List<TripRestaurantResponseDTO> visits =
            tripRestaurantService.getRestaurantsForTrip(
                trip.getId(),
                "alice"
            );

        assertEquals(2, visits.size());
        assertEquals(LocalDate.of(2026, 6, 11), visits.get(0).visitDate());
        assertEquals(LocalDate.of(2026, 6, 12), visits.get(1).visitDate());
    }

    @Test
    void removeRestaurantFromTrip_shouldDeleteVisitFromDatabase() {
        UUID visitId = tripRestaurantService.addRestaurantToTrip(
            trip.getId(),
            "alice",
            restaurant.getId(),
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        tripRestaurantService.removeRestaurantFromTrip(
            trip.getId(),
            visitId,
            "alice"
        );

        assertFalse(tripRestaurantRepository.findById(visitId).isPresent());
    }
}
