package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.TripRestaurant;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import integra.vacation_planner_backend.repository.FlightRepository;
import integra.vacation_planner_backend.repository.RestaurantRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.repository.TripRestaurantRepository;
import integra.vacation_planner_backend.repository.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TripDetailsIntegrationTest {

    private static final LocalDate START = LocalDate.of(2027, 6, 10);

    private static final LocalDate END = LocalDate.of(2027, 6, 15);

    // midday UTC, so the flight is on the same day in almost every time zone
    private static final Instant DEPARTURE = Instant.parse("2027-06-10T12:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TripRestaurantRepository tripRestaurantRepository;

    private final List<UUID> userIds = new ArrayList<>();

    private final List<UUID> tripIds = new ArrayList<>();

    private final List<UUID> flightIds = new ArrayList<>();

    private final List<UUID> accommodationIds = new ArrayList<>();

    private final List<UUID> restaurantIds = new ArrayList<>();

    private final List<UUID> visitIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        // the order matters: first the things that point to other things
        tripRestaurantRepository.deleteAllById(visitIds);
        tripRepository.deleteAllById(tripIds);
        flightRepository.deleteAllById(flightIds);
        accommodationRepository.deleteAllById(accommodationIds);
        restaurantRepository.deleteAllById(restaurantIds);
        userRepository.deleteAllById(userIds);
        visitIds.clear();
        tripIds.clear();
        flightIds.clear();
        accommodationIds.clear();
        restaurantIds.clear();
        userIds.clear();
    }

    @Test
    void returnsTheWholeTripInOneResponse() throws Exception {
        User owner = saveUser();
        Trip trip = saveTrip(owner, 2);

        mockMvc.perform(get("/trips/" + trip.getId()).param("username", owner.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(trip.getId().toString()))
            .andExpect(jsonPath("$.destination").value("Paris"))
            .andExpect(jsonPath("$.startDate").value("2027-06-10"))
            .andExpect(jsonPath("$.endDate").value("2027-06-15"))
            .andExpect(jsonPath("$.owner.username").value(owner.getUsername()))
            .andExpect(jsonPath("$.owner.firstName").value("Test"))
            .andExpect(jsonPath("$.owner.lastName").value("User"))
            .andExpect(jsonPath("$.accommodations.length()").value(2))
            .andExpect(jsonPath("$.accommodations[0].name").value("Hotel 0"))
            .andExpect(jsonPath("$.flights.length()").value(2))
            .andExpect(jsonPath("$.restaurants.length()").value(2))
            // 10 June ... 15 June = 6 days
            .andExpect(jsonPath("$.timeline.length()").value(6))
            .andExpect(jsonPath("$.timeline[0].date").value("2027-06-10"))
            .andExpect(jsonPath("$.timeline[0].flights.length()").value(2))
            .andExpect(jsonPath("$.timeline[0].restaurants.length()").value(1))
            .andExpect(jsonPath("$.timeline[1].restaurants.length()").value(1))
            .andExpect(jsonPath("$.timeline[2].flights").isEmpty());
    }

    @Test
    void aTripWithNothingLinked_returnsEmptyListsAndAllTheDays() throws Exception {
        User owner = saveUser();
        Trip trip = saveTrip(owner, 0);

        mockMvc.perform(get("/trips/" + trip.getId()).param("username", owner.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accommodations").isArray())
            .andExpect(jsonPath("$.accommodations").isEmpty())
            .andExpect(jsonPath("$.flights").isArray())
            .andExpect(jsonPath("$.flights").isEmpty())
            .andExpect(jsonPath("$.restaurants").isArray())
            .andExpect(jsonPath("$.restaurants").isEmpty())
            .andExpect(jsonPath("$.timeline.length()").value(6));
    }

    @Test
    void unknownTrip_returns404() throws Exception {
        mockMvc.perform(get("/trips/" + UUID.randomUUID()).param("username", "anyone"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));
    }

    @Test
    void notTheOwner_returns403() throws Exception {
        Trip trip = saveTrip(saveUser(), 1);
        User other = saveUser();

        mockMvc.perform(get("/trips/" + trip.getId()).param("username", other.getUsername()))
            .andExpect(status().isForbidden());
    }

    @Test
    void theNumberOfSqlStatementsDoesNotGrowWithTheNumberOfLinks() throws Exception {
        User owner = saveUser();
        Trip smallTrip = saveTrip(owner, 1);
        Trip bigTrip = saveTrip(owner, 4);

        long statementsForSmallTrip = countStatements(smallTrip, owner);
        long statementsForBigTrip = countStatements(bigTrip, owner);

        // With N+1 queries the big trip would need many more statements.
        assertThat(statementsForBigTrip).isEqualTo(statementsForSmallTrip);
    }

    private long countStatements(Trip trip, User owner) throws Exception {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        mockMvc.perform(get("/trips/" + trip.getId()).param("username", owner.getUsername()))
            .andExpect(status().isOk());

        return statistics.getPrepareStatementCount();
    }

    // ------------------------------------------------------------------
    // helpers that put test data in the database
    // ------------------------------------------------------------------

    private User saveUser() {
        // the username column is only 20 characters long
        String username = "det-" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.local");
        user.setFirstName("Test");
        user.setLastName("User");
        User saved = userRepository.save(user);
        userIds.add(saved.getId());
        return saved;
    }

    // A trip with "links" accommodations, "links" flights and "links" restaurant visits
    private Trip saveTrip(User owner, int links) {
        Trip trip = new Trip();
        trip.setDestination("Paris");
        trip.setStartDate(START);
        trip.setEndDate(END);
        trip.setUser(owner);

        for (int i = 0; i < links; i++) {
            trip.getAccommodations().add(saveAccommodation("Hotel " + i));
            trip.getFlights().add(saveFlight(i));
        }

        Trip saved = tripRepository.save(trip);
        tripIds.add(saved.getId());

        for (int i = 0; i < links; i++) {
            saveVisit(saved, i);
        }

        return saved;
    }

    private Accommodation saveAccommodation(String name) {
        Accommodation accommodation = new Accommodation(
            name, new BigDecimal("100.00"), "Some street 1", "Paris"
        );
        Accommodation saved = accommodationRepository.save(accommodation);
        accommodationIds.add(saved.getId());
        return saved;
    }

    private Flight saveFlight(int index) {
        Flight flight = new Flight();
        flight.setFlightNumber("T" + UUID.randomUUID().toString().substring(0, 8));
        flight.setDepartureAirportCode("CLJ");
        flight.setArrivalAirportCode("CDG");
        flight.setDepartureTime(DEPARTURE.plusSeconds(3600L * index));
        flight.setArrivalTime(DEPARTURE.plusSeconds(3600L * index + 7200));
        flight.setNumberOfSeats(100);
        flight.setPrice(new BigDecimal("150.00"));
        Flight saved = flightRepository.save(flight);
        flightIds.add(saved.getId());
        return saved;
    }

    // visit number "index" is on day "index" of the trip, at 12:00 + index hours
    private void saveVisit(Trip trip, int index) {
        Restaurant restaurant = restaurantRepository.save(new Restaurant(
            UUID.randomUUID(), "Restaurant " + index, "Some street 1",
            LocalTime.of(9, 0), LocalTime.of(23, 0)
        ));
        restaurantIds.add(restaurant.getId());

        TripRestaurant visit = tripRestaurantRepository.save(new TripRestaurant(
            trip, restaurant, START.plusDays(index), LocalTime.of(12 + index, 0)
        ));
        visitIds.add(visit.getId());
    }
}
