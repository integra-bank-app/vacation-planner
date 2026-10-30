package integra.vacation_planner_backend.controller;

import com.jayway.jsonpath.JsonPath;
import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.AddFlightToTripRequest;
import integra.vacation_planner_backend.dto.TripAccommodationRequest;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import integra.vacation_planner_backend.repository.FlightRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack tests: HTTP -> controller -> service -> JPA -> H2 (Flyway migrations applied).
 * Nothing is mocked. Each test uses unique usernames / flight numbers and deletes what it
 * created, so the sample data seeded by the migrations is left alone.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TripControllerTest {

    private static final LocalDate START = LocalDate.of(2027, 6, 10);

    private static final LocalDate END = LocalDate.of(2027, 6, 15);

    private static final Instant OUT_DEP = Instant.parse("2027-06-10T05:00:00Z");

    private static final Instant OUT_ARR = Instant.parse("2027-06-10T07:30:00Z");

    private static final Instant IN_DEP = Instant.parse("2027-06-15T10:00:00Z");

    private static final Instant IN_ARR = Instant.parse("2027-06-15T13:30:00Z");

    private static final String DATE_ORDER_ERROR = "startDate must be before endDate";

    // an existing user, for tests that need a valid username but are about something else
    private static final String VALID_USER =
        "validator-" + UUID.randomUUID().toString().substring(0, 8);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    private final List<UUID> createdTripIds = new ArrayList<>();

    private final List<UUID> createdFlightIds = new ArrayList<>();

    private final List<UUID> createdAccommodationIds = new ArrayList<>();

    private final List<UUID> createdUserIds = new ArrayList<>();

    @BeforeEach
    void createValidUser() {
        if (!userRepository.existsByUsername(VALID_USER)) {
            createUser(VALID_USER);
        }
    }

    @AfterEach
    void cleanUp() {
        // trips first: they reference flights and accommodations through join tables
        tripRepository.deleteAllById(createdTripIds);
        flightRepository.deleteAllById(createdFlightIds);
        accommodationRepository.deleteAllById(createdAccommodationIds);
        userRepository.deleteAllById(createdUserIds);
        createdTripIds.clear();
        createdFlightIds.clear();
        createdAccommodationIds.clear();
        createdUserIds.clear();
    }

    // ------------------------------------------------------------------
    // Full flow
    // ------------------------------------------------------------------

    @Test
    void fullFlow_createAddFlightsUpdateRemoveFlightAndDeleteTrip() throws Exception {
        String user = user("alice");
        Flight outbound = saveFlight(unique("OUT"), OUT_DEP, OUT_ARR, 5);
        Flight inbound = saveFlight(unique("IN"), IN_DEP, IN_ARR, 5);

        UUID tripId = createTrip(user, "Paris", START, END);
        assertThat(tripRepository.findById(tripId)).isPresent();

        // return flight added first, to prove the response is sorted by departure time
        addFlight(tripId, user, inbound.getFlightNumber()).andExpect(status().isNoContent());
        addFlight(tripId, user, outbound.getFlightNumber()).andExpect(status().isNoContent());

        String flightsJson = getFlights(tripId, user)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].flightNumber").value(outbound.getFlightNumber()))
            .andExpect(jsonPath("$[0].departureAirport").value("CLJ"))
            .andExpect(jsonPath("$[0].arrivalAirport").value("CDG"))
            .andExpect(jsonPath("$[0].price").value(150.00))
            .andExpect(jsonPath("$[1].flightNumber").value(inbound.getFlightNumber()))
            .andReturn().getResponse().getContentAsString();
        assertThat(instantAt(flightsJson, "$[0].departureTime")).isEqualTo(OUT_DEP);
        assertThat(instantAt(flightsJson, "$[1].arrivalTime")).isEqualTo(IN_ARR);

        searchTrips(user, "2027-06-01", "2027-06-30")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(tripId.toString()))
            .andExpect(jsonPath("$[0].destination").value("Paris"))
            .andExpect(jsonPath("$[0].startDate").value("2027-06-10"))
            .andExpect(jsonPath("$[0].endDate").value("2027-06-15"));

        putTrip(tripId, tripDto(user, null, null, LocalDate.of(2027, 6, 20)))
            .andExpect(status().isOk());

        removeFlight(tripId, outbound.getFlightNumber(), user).andExpect(status().isOk());
        getFlights(tripId, user)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].flightNumber").value(inbound.getFlightNumber()));
        assertThat(flightRepository.findByFlightNumber(outbound.getFlightNumber())).isPresent();

        deleteTrip(tripId, user).andExpect(status().isOk());
        assertThat(tripRepository.existsById(tripId)).isFalse();
        assertThat(tripRepository.countTripsUsingFlight(inbound.getId())).isZero();
        assertThat(flightRepository.findByFlightNumber(inbound.getFlightNumber())).isPresent();
        getFlights(tripId, user).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // POST /trips
    // ------------------------------------------------------------------

    @Test
    void createTrip_persistsTheTripWithItsOwner() throws Exception {
        String user = user("bob");

        UUID tripId = createTrip(user, "Rome", START, END);

        assertThat(tripRepository.findById(tripId)).hasValueSatisfying(trip -> {
            assertThat(trip.getDestination()).isEqualTo("Rome");
            assertThat(trip.getStartDate()).isEqualTo(START);
            assertThat(trip.getEndDate()).isEqualTo(END);
        });

        // the owner is checked through the API (Trip.user is lazy, so not read from the entity)
        searchTrips(user, "2027-06-01", "2027-06-30")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value(user));
    }

    @Test
    void createTrip_acceptsASingleDayTrip() throws Exception {
        UUID oneDay = createTrip(user("bob"), "Rome", START, START);

        assertThat(tripRepository.findById(oneDay)).hasValueSatisfying(trip -> {
            assertThat(trip.getStartDate()).isEqualTo(START);
            assertThat(trip.getEndDate()).isEqualTo(START);
        });
    }

    @ParameterizedTest
    @MethodSource("invalidNewTrips")
    void createTrip_isRejectedWithBadRequest(
        String username, String destination, LocalDate start, LocalDate end, String message
    ) throws Exception {
        postTrip(tripDto(username, destination, start, end))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(message));
    }

    static Stream<Arguments> invalidNewTrips() {
        return Stream.of(
            Arguments.of(null, "Paris", START, END, "username is required"),
            Arguments.of("  ", "Paris", START, END, "username is required"),
            Arguments.of(VALID_USER, null, START, END, "destination is required"),
            Arguments.of(VALID_USER, " ", START, END, "destination is required"),
            Arguments.of(VALID_USER, "Paris", null, END, "startDate and endDate are required"),
            Arguments.of(VALID_USER, "Paris", START, null, "startDate and endDate are required"),
            Arguments.of(VALID_USER, "Paris", END, START, DATE_ORDER_ERROR)
        );
    }

    // ------------------------------------------------------------------
    // GET /trips
    // ------------------------------------------------------------------

    @Test
    void getTrips_returnsOnlyTheUsersTripsInsideTheRange() throws Exception {
        String user = userWithThreeTrips();

        searchTrips(user, "2027-06-01", "2027-07-31")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[?(@.destination == 'Paris')]").exists())
            .andExpect(jsonPath("$[?(@.destination == 'Rome')]").exists());
    }

    @Test
    void getTrips_includesATripThatMatchesTheRangeBoundariesExactly() throws Exception {
        String user = userWithThreeTrips();

        searchTrips(user, "2027-06-10", "2027-06-15")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].destination").value("Paris"));
    }

    @Test
    void getTrips_excludesATripThatStartsBeforeTheRange() throws Exception {
        String user = userWithThreeTrips();

        searchTrips(user, "2027-06-12", "2027-06-30")
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void getTrips_excludesATripThatEndsAfterTheRange() throws Exception {
        String user = userWithThreeTrips();

        searchTrips(user, "2027-06-01", "2027-06-12")
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void getTrips_doesNotExposeTheFlightsCollection() throws Exception {
        String user = user("carol");
        UUID tripId = createTrip(user, "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);
        addFlight(tripId, user, flight.getFlightNumber()).andExpect(status().isNoContent());

        searchTrips(user, "2027-06-01", "2027-06-30")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(tripId.toString()))
            .andExpect(jsonPath("$[0].flights").doesNotExist());
    }

    @Test
    void getTrips_rejectsAReversedRange() throws Exception {
        searchTrips(VALID_USER, "2027-06-30", "2027-06-01")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(DATE_ORDER_ERROR));
    }

    @Test
    void getTrips_rejectsABlankUsername() throws Exception {
        searchTrips(" ", "2027-06-01", "2027-06-30")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("username is required"));
    }

    // ------------------------------------------------------------------
    // PUT /trips/{id}
    // ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("validUpdates")
    void updateTrip_changesOnlyTheDatesSent(
        LocalDate newStart, LocalDate newEnd, LocalDate expectedStart, LocalDate expectedEnd
    ) throws Exception {
        String user = user("erin");
        UUID tripId = createTrip(user, "Paris", START, END);

        putTrip(tripId, tripDto(user, null, newStart, newEnd)).andExpect(status().isOk());

        assertThat(tripRepository.findById(tripId)).hasValueSatisfying(trip -> {
            assertThat(trip.getStartDate()).isEqualTo(expectedStart);
            assertThat(trip.getEndDate()).isEqualTo(expectedEnd);
        });
    }

    static Stream<Arguments> validUpdates() {
        LocalDate jul1 = LocalDate.of(2027, 7, 1);
        LocalDate jul5 = LocalDate.of(2027, 7, 5);
        LocalDate jun12 = LocalDate.of(2027, 6, 12);
        LocalDate jun20 = LocalDate.of(2027, 6, 20);
        return Stream.of(
            Arguments.of(jun12, null, jun12, END),
            Arguments.of(null, jun20, START, jun20),
            Arguments.of(jul1, jul5, jul1, jul5)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidUpdates")
    void updateTrip_isRejected_andLeavesTheTripUntouched(
        LocalDate newStart, LocalDate newEnd, String message
    ) throws Exception {
        String user = user("erin");
        UUID tripId = createTrip(user, "Paris", START, END);

        putTrip(tripId, tripDto(user, null, newStart, newEnd))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(message));

        assertThat(tripRepository.findById(tripId)).hasValueSatisfying(trip -> {
            assertThat(trip.getStartDate()).isEqualTo(START);
            assertThat(trip.getEndDate()).isEqualTo(END);
        });
    }

    static Stream<Arguments> invalidUpdates() {
        return Stream.of(
            Arguments.of(null, null, "At least one date must be provided"),
            // new start is after the stored end
            Arguments.of(LocalDate.of(2027, 6, 20), null, DATE_ORDER_ERROR),
            // new end is before the stored start
            Arguments.of(null, LocalDate.of(2027, 6, 5), DATE_ORDER_ERROR),
            Arguments.of(LocalDate.of(2027, 7, 5), LocalDate.of(2027, 7, 1), DATE_ORDER_ERROR)
        );
    }

    @Test
    void updateTrip_ignoresDestination_documentingThatItCannotBeChanged() throws Exception {
        String user = user("erin");
        UUID tripId = createTrip(user, "Paris", START, END);

        putTrip(tripId, tripDto(user, "Rome", null, LocalDate.of(2027, 6, 20)))
            .andExpect(status().isOk());

        assertThat(tripRepository.findById(tripId))
            .hasValueSatisfying(trip -> assertThat(trip.getDestination()).isEqualTo("Paris"));
    }

    @Test
    void updateTrip_returnsForbiddenForANonOwner_andLeavesTheTripUntouched() throws Exception {
        UUID tripId = createTrip(user("erin"), "Paris", START, END);

        putTrip(tripId, tripDto(user("mallory"), null, LocalDate.of(2027, 6, 12), null))
            .andExpect(status().isForbidden())
            .andExpect(content().string("Not allowed to update this trip"));

        assertThat(tripRepository.findById(tripId))
            .hasValueSatisfying(trip -> assertThat(trip.getStartDate()).isEqualTo(START));
    }

    @Test
    void updateTrip_returnsNotFoundForAnUnknownTrip() throws Exception {
        putTrip(UUID.randomUUID(), tripDto(VALID_USER, null, LocalDate.of(2027, 6, 12), null))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void updateTrip_rejectsAMissingUsername(String username) throws Exception {
        UUID tripId = createTrip(user("erin"), "Paris", START, END);

        putTrip(tripId, tripDto(username, null, LocalDate.of(2027, 6, 12), null))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("username is required"));
    }

    // ------------------------------------------------------------------
    // DELETE /trips/{id}
    // ------------------------------------------------------------------

    @Test
    void deleteTrip_withFlights_freesTheSeatAndKeepsTheFlight() throws Exception {
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 1);
        String first = user("erin");
        String second = user("frank");
        UUID firstTrip = createTrip(first, "Paris", START, END);
        UUID secondTrip = createTrip(second, "Paris", START, END);
        addFlight(firstTrip, first, flight.getFlightNumber()).andExpect(status().isNoContent());
        addFlight(secondTrip, second, flight.getFlightNumber()).andExpect(status().isConflict());

        deleteTrip(firstTrip, first).andExpect(status().isOk());

        assertThat(flightRepository.findByFlightNumber(flight.getFlightNumber())).isPresent();
        addFlight(secondTrip, second, flight.getFlightNumber()).andExpect(status().isNoContent());
    }

    @Test
    void deleteTrip_returnsForbiddenForANonOwner_andKeepsTheTrip() throws Exception {
        UUID tripId = createTrip(user("erin"), "Paris", START, END);

        deleteTrip(tripId, user("mallory"))
            .andExpect(status().isForbidden())
            .andExpect(content().string("Not allowed to delete this trip"));

        assertThat(tripRepository.existsById(tripId)).isTrue();
    }

    @Test
    void deleteTrip_rejectsABlankUsername_andKeepsTheTrip() throws Exception {
        UUID tripId = createTrip(user("erin"), "Paris", START, END);

        deleteTrip(tripId, " ")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("username is required"));

        assertThat(tripRepository.existsById(tripId)).isTrue();
    }

    @Test
    void deleteTrip_returnsNotFoundForAnUnknownTrip() throws Exception {
        deleteTrip(UUID.randomUUID(), VALID_USER)
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));
    }

    // ------------------------------------------------------------------
    // POST /trips/{id}/flights
    // ------------------------------------------------------------------

    @Test
    void addFlight_returnsConflictWhenTheTripAlreadyHasTheFlight() throws Exception {
        String user = user("frank");
        UUID tripId = createTrip(user, "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);
        addFlight(tripId, user, flight.getFlightNumber()).andExpect(status().isNoContent());

        addFlight(tripId, user, flight.getFlightNumber()).andExpect(status().isConflict());

        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isEqualTo(1);
    }

    @Test
    void addFlight_allowsTheSameFlightOnDifferentTrips() throws Exception {
        String user = user("frank");
        UUID firstTrip = createTrip(user, "Paris", START, END);
        UUID secondTrip = createTrip(user, "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);

        addFlight(firstTrip, user, flight.getFlightNumber()).andExpect(status().isNoContent());
        addFlight(secondTrip, user, flight.getFlightNumber()).andExpect(status().isNoContent());

        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isEqualTo(2);
    }

    @Test
    void addFlight_fillsTheFlightToCapacity_thenReturnsConflict() throws Exception {
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 2);
        List<String> users = List.of(user("u1"), user("u2"), user("u3"));
        List<UUID> trips = new ArrayList<>();
        for (String user : users) {
            trips.add(createTrip(user, "Paris", START, END));
        }

        addFlight(trips.get(0), users.get(0), flight.getFlightNumber())
            .andExpect(status().isNoContent());
        addFlight(trips.get(1), users.get(1), flight.getFlightNumber())
            .andExpect(status().isNoContent());
        addFlight(trips.get(2), users.get(2), flight.getFlightNumber())
            .andExpect(status().isConflict());

        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isEqualTo(2);
    }

    @Test
    void addFlight_returnsForbiddenForANonOwner_andBooksNothing() throws Exception {
        UUID tripId = createTrip(user("frank"), "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);

        addFlight(tripId, user("mallory"), flight.getFlightNumber())
            .andExpect(status().isForbidden())
            .andExpect(content().string("Not allowed to add flights to this trip"));

        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isZero();
    }

    @Test
    void addFlight_returnsNotFoundForAnUnknownTrip() throws Exception {
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);

        addFlight(UUID.randomUUID(), VALID_USER, flight.getFlightNumber())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));
    }

    @Test
    void addFlight_returnsNotFoundForAnUnknownFlight() throws Exception {
        String owner = user("frank");
        UUID tripId = createTrip(owner, "Paris", START, END);

        addFlight(tripId, owner, unique("NOPE")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addFlight_rejectsAMissingFlightNumber(String flightNumber) throws Exception {
        String user = user("frank");
        UUID tripId = createTrip(user, "Paris", START, END);

        addFlight(tripId, user, flightNumber)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("flightNumber is required"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addFlight_rejectsAMissingUsername(String username) throws Exception {
        UUID tripId = createTrip(user("frank"), "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);

        addFlight(tripId, username, flight.getFlightNumber())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("username is required"));
        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isZero();
    }

    @Test
    void addFlight_neverOverbooks_whenManyUsersRaceForTheLastSeat() throws Exception {
        // Exercises the PESSIMISTIC_WRITE lock in findByFlightNumberForUpdate: without it,
        // several requests could read "0 booked" at once and all succeed.
        int contenders = 5;
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 1);
        List<String> users = new ArrayList<>();
        List<UUID> trips = new ArrayList<>();
        for (int i = 0; i < contenders; i++) {
            String user = user("racer");
            users.add(user);
            trips.add(createTrip(user, "Paris", START, END));
        }

        ExecutorService pool = Executors.newFixedThreadPool(contenders);
        CountDownLatch ready = new CountDownLatch(contenders);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();
        List<Integer> statuses = new ArrayList<>();
        try {
            for (int i = 0; i < contenders; i++) {
                final int index = i;
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    return addFlight(trips.get(index), users.get(index), flight.getFlightNumber())
                        .andReturn().getResponse().getStatus();
                }));
            }
            ready.await();
            go.countDown();
            for (Future<Integer> future : futures) {
                statuses.add(future.get(30, TimeUnit.SECONDS));
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(Collections.frequency(statuses, 204)).isEqualTo(1);
        assertThat(Collections.frequency(statuses, 409)).isEqualTo(contenders - 1);
        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isEqualTo(1);
    }

    // ------------------------------------------------------------------
    // GET /trips/{id}/flights
    // ------------------------------------------------------------------

    @Test
    void getFlights_returnsEmptyListForATripWithNoFlights() throws Exception {
        String user = user("ivan");
        UUID tripId = createTrip(user, "Paris", START, END);

        getFlights(tripId, user)
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void getFlights_returnsForbiddenForANonOwner() throws Exception {
        UUID tripId = createTrip(user("ivan"), "Paris", START, END);

        getFlights(tripId, user("mallory"))
            .andExpect(status().isForbidden())
            .andExpect(content().string(
                "User is not authorized to view the flights for this trip"));
    }

    @Test
    void getFlights_returnsNotFoundForAnUnknownTrip() throws Exception {
        getFlights(UUID.randomUUID(), VALID_USER)
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));
    }

    @Test
    void getFlights_rejectsABlankUsername() throws Exception {
        UUID tripId = createTrip(user("ivan"), "Paris", START, END);

        getFlights(tripId, " ")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Username is required"));
    }

    // ------------------------------------------------------------------
    // DELETE /trips/{id}/flights/{flightNumber}
    // ------------------------------------------------------------------

    @Test
    void removeFlight_unlinksOnlyThatFlightFromOnlyThatTrip_andFreesTheSeat() throws Exception {
        String user = user("judy");
        String other = user("karl");
        UUID tripId = createTrip(user, "Paris", START, END);
        UUID otherTrip = createTrip(other, "Paris", START, END);
        Flight shared = saveFlight(unique("SH"), OUT_DEP, OUT_ARR, 2);
        Flight kept = saveFlight(unique("KEEP"), IN_DEP, IN_ARR, 5);
        addFlight(tripId, user, shared.getFlightNumber()).andExpect(status().isNoContent());
        addFlight(tripId, user, kept.getFlightNumber()).andExpect(status().isNoContent());
        addFlight(otherTrip, other, shared.getFlightNumber()).andExpect(status().isNoContent());

        removeFlight(tripId, shared.getFlightNumber(), user).andExpect(status().isOk());

        // only this trip lost it; the other trip and the flight record are intact
        assertThat(tripRepository.countTripsUsingFlight(shared.getId())).isEqualTo(1);
        assertThat(tripRepository.countTripsUsingFlight(kept.getId())).isEqualTo(1);
        assertThat(flightRepository.findByFlightNumber(shared.getFlightNumber())).isPresent();
    }

    @Test
    void removeFlight_returnsNotFoundWhenRemovingTheSameFlightTwice() throws Exception {
        String user = user("judy");
        UUID tripId = createTrip(user, "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);
        addFlight(tripId, user, flight.getFlightNumber()).andExpect(status().isNoContent());
        removeFlight(tripId, flight.getFlightNumber(), user).andExpect(status().isOk());

        removeFlight(tripId, flight.getFlightNumber(), user).andExpect(status().isNotFound());
    }

    @Test
    void removeFlight_returnsForbiddenForANonOwner_andKeepsTheFlight() throws Exception {
        String owner = user("judy");
        UUID tripId = createTrip(owner, "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);
        addFlight(tripId, owner, flight.getFlightNumber()).andExpect(status().isNoContent());

        removeFlight(tripId, flight.getFlightNumber(), user("mallory"))
            .andExpect(status().isForbidden())
            .andExpect(content().string(
                "User is not authorized to remove flights from this trip"));

        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isEqualTo(1);
    }

    @Test
    void removeFlight_returnsNotFoundForAnUnknownTrip() throws Exception {
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);

        removeFlight(UUID.randomUUID(), flight.getFlightNumber(), VALID_USER)
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));
    }

    @Test
    void removeFlight_returnsNotFoundForAnUnknownFlight() throws Exception {
        String owner = user("judy");
        UUID tripId = createTrip(owner, "Paris", START, END);

        removeFlight(tripId, unique("NOPE"), owner).andExpect(status().isNotFound());
    }

    @Test
    void removeFlight_returnsNotFoundForAFlightThatIsNotOnTheTrip() throws Exception {
        String owner = user("judy");
        UUID tripId = createTrip(owner, "Paris", START, END);
        Flight notOnTrip = saveFlight(unique("FL"), IN_DEP, IN_ARR, 5);

        removeFlight(tripId, notOnTrip.getFlightNumber(), owner)
            .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Unknown user (replaces the old mocked UserNotFoundException tests)
    // ------------------------------------------------------------------

    @Test
    void createTrip_returnsNotFound_whenUserDoesNotExist() throws Exception {
        postTrip(tripDto(unique("ghost"), "Paris", START, END))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    void getTrips_returnsNotFound_whenUserDoesNotExist() throws Exception {
        searchTrips(unique("ghost"), "2027-06-01", "2027-06-30")
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("User not found"));
    }

    // ------------------------------------------------------------------
    // Accommodations on a trip (/trips/{id}/accommodations)
    // ------------------------------------------------------------------

    @Test
    void accommodations_fullFlow_addListAndRemove() throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);
        Accommodation hotel = saveAccommodation("Hotel Central", "Str. Memorandumului 10", "Cluj");

        getAccommodations(tripId, owner)
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        addAccommodation(tripId, owner, hotel.getId()).andExpect(status().isOk());

        getAccommodations(tripId, owner)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(hotel.getId().toString()))
            .andExpect(jsonPath("$[0].name").value("Hotel Central"))
            .andExpect(jsonPath("$[0].address").value("Str. Memorandumului 10"))
            .andExpect(jsonPath("$[0].city").value("Cluj"))
            .andExpect(jsonPath("$[0].pricePerNight").value(250));

        removeAccommodation(tripId, hotel.getId(), owner).andExpect(status().isOk());

        getAccommodations(tripId, owner).andExpect(content().json("[]"));
        // the accommodation itself is kept
        assertThat(accommodationRepository.existsById(hotel.getId())).isTrue();
    }

    @Test
    void addAccommodation_returnsConflictWhenTheTripAlreadyHasIt() throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);
        Accommodation hotel = saveHotel();
        addAccommodation(tripId, owner, hotel.getId()).andExpect(status().isOk());

        addAccommodation(tripId, owner, hotel.getId()).andExpect(status().isConflict());

        getAccommodations(tripId, owner).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addAccommodation_allowsTheSameAccommodationOnAnotherTrip() throws Exception {
        String owner = user("lena");
        UUID firstTrip = createTrip(owner, "Cluj", START, END);
        UUID secondTrip = createTrip(owner, "Cluj", START, END);
        Accommodation hotel = saveHotel();

        addAccommodation(firstTrip, owner, hotel.getId()).andExpect(status().isOk());
        addAccommodation(secondTrip, owner, hotel.getId()).andExpect(status().isOk());
    }

    @Test
    void addAccommodation_returnsForbiddenForANonOwner_andAddsNothing() throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);
        Accommodation hotel = saveHotel();

        addAccommodation(tripId, user("mallory"), hotel.getId())
            .andExpect(status().isForbidden());

        getAccommodations(tripId, owner).andExpect(content().json("[]"));
    }

    @Test
    void addAccommodation_returnsNotFoundForAnUnknownTrip() throws Exception {
        Accommodation hotel = saveHotel();

        addAccommodation(UUID.randomUUID(), VALID_USER, hotel.getId())
            .andExpect(status().isNotFound());
    }

    @Test
    void addAccommodation_returnsNotFoundForAnUnknownAccommodation() throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);

        addAccommodation(tripId, owner, UUID.randomUUID()).andExpect(status().isNotFound());
    }

    @Test
    void getAccommodations_returnsForbiddenForANonOwner() throws Exception {
        UUID tripId = createTrip(user("lena"), "Cluj", START, END);

        getAccommodations(tripId, user("mallory")).andExpect(status().isForbidden());
    }

    @Test
    void getAccommodations_returnsNotFoundForAnUnknownTrip() throws Exception {
        getAccommodations(UUID.randomUUID(), VALID_USER).andExpect(status().isNotFound());
    }

    @Test
    void removeAccommodation_returnsForbiddenForANonOwner_andKeepsIt() throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);
        Accommodation hotel = saveHotel();
        addAccommodation(tripId, owner, hotel.getId()).andExpect(status().isOk());

        removeAccommodation(tripId, hotel.getId(), user("mallory"))
            .andExpect(status().isForbidden());

        getAccommodations(tripId, owner).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void removeAccommodation_returnsNotFoundForAnUnknownTrip() throws Exception {
        Accommodation hotel = saveHotel();

        removeAccommodation(UUID.randomUUID(), hotel.getId(), VALID_USER)
            .andExpect(status().isNotFound());
    }

    @Test
    void removeAccommodation_returnsNotFoundForAnUnknownAccommodation() throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);

        removeAccommodation(tripId, UUID.randomUUID(), owner).andExpect(status().isNotFound());
    }

    @Test
    void removeAccommodation_returnsNotFoundForAnAccommodationThatIsNotOnTheTrip()
        throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);
        Accommodation notOnTrip = saveHotel();

        removeAccommodation(tripId, notOnTrip.getId(), owner).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Requests Spring rejects before the service is reached
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("malformedRequests")
    void malformedRequests_areRejectedWithBadRequest(String description, RequestBuilder request)
        throws Exception {
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    static Stream<Arguments> malformedRequests() {
        MediaType json = MediaType.APPLICATION_JSON;
        return Stream.of(
            Arguments.of("POST /trips with invalid JSON",
                post("/trips").contentType(json).content("{ this is not json")),
            Arguments.of("POST /trips with no body",
                post("/trips").contentType(json)),
            Arguments.of("POST /trips with an impossible date",
                post("/trips").contentType(json).content(
                    "{\"username\":\"bob\",\"destination\":\"Paris\","
                        + "\"startDate\":\"2027-13-45\",\"endDate\":\"2027-06-15\"}")),
            Arguments.of("PUT /trips/{id} with an invalid trip id",
                put("/trips/not-a-uuid").contentType(json).content("{}")),
            Arguments.of("POST /trips/{id}/flights with an invalid trip id",
                post("/trips/not-a-uuid/flights").contentType(json).content("{}")),
            Arguments.of("GET /trips/{id}/flights with an invalid trip id",
                get("/trips/not-a-uuid/flights").param("username", "bob")),
            Arguments.of("GET /trips/{id}/flights without username",
                get("/trips/" + UUID.randomUUID() + "/flights")),
            Arguments.of("DELETE /trips/{id} with an invalid trip id",
                delete("/trips/not-a-uuid").param("username", "bob")),
            Arguments.of("DELETE /trips/{id} without username",
                delete("/trips/" + UUID.randomUUID())),
            Arguments.of("DELETE /trips/{id}/flights/{n} with an invalid trip id",
                delete("/trips/not-a-uuid/flights/RO101").param("username", "bob")),
            Arguments.of("GET /trips without username",
                get("/trips").param("startDate", "2027-06-01").param("endDate", "2027-06-30")),
            Arguments.of("GET /trips without startDate",
                get("/trips").param("username", "bob").param("endDate", "2027-06-30")),
            Arguments.of("GET /trips without endDate",
                get("/trips").param("username", "bob").param("startDate", "2027-06-01")),
            Arguments.of("GET /trips with a startDate that is not a date",
                get("/trips").param("username", "bob").param("startDate", "not-a-date")
                    .param("endDate", "2027-06-30"))
        );
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /** A user with trips in Jun, Jul and Aug 2027, plus an unrelated user's trip in Jun. */
    private String userWithThreeTrips() throws Exception {
        String user = user("carol");
        createTrip(user, "Paris", LocalDate.of(2027, 6, 10), LocalDate.of(2027, 6, 15));
        createTrip(user, "Rome", LocalDate.of(2027, 7, 1), LocalDate.of(2027, 7, 5));
        createTrip(user, "Oslo", LocalDate.of(2027, 8, 10), LocalDate.of(2027, 8, 20));
        createTrip(user("dave"), "Madrid", LocalDate.of(2027, 6, 10), LocalDate.of(2027, 6, 15));
        return user;
    }

    private Accommodation saveHotel() {
        return saveAccommodation("Hotel Central", "Str. Memorandumului 10", "Cluj");
    }

    private UUID createTrip(String username, String destination, LocalDate start, LocalDate end)
        throws Exception {
        MvcResult result = postTrip(tripDto(username, destination, start, end))
            .andExpect(status().isOk())
            .andReturn();

        UUID id = objectMapper.readValue(result.getResponse().getContentAsString(), UUID.class);
        createdTripIds.add(id);
        return id;
    }

    private ResultActions postTrip(TripDTO dto) throws Exception {
        return mockMvc.perform(post("/trips")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions putTrip(UUID tripId, TripDTO dto) throws Exception {
        return mockMvc.perform(put("/trips/" + tripId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions deleteTrip(UUID tripId, String username) throws Exception {
        return mockMvc.perform(delete("/trips/" + tripId).param("username", username));
    }

    private ResultActions searchTrips(String username, String startDate, String endDate)
        throws Exception {
        return mockMvc.perform(get("/trips")
            .param("username", username)
            .param("startDate", startDate)
            .param("endDate", endDate));
    }

    private ResultActions addFlight(UUID tripId, String username, String flightNumber)
        throws Exception {
        AddFlightToTripRequest request = new AddFlightToTripRequest();
        request.setUsername(username);
        request.setFlightNumber(flightNumber);

        return mockMvc.perform(post("/trips/" + tripId + "/flights")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions getFlights(UUID tripId, String username) throws Exception {
        return mockMvc.perform(get("/trips/" + tripId + "/flights").param("username", username));
    }

    private ResultActions removeFlight(UUID tripId, String flightNumber, String username)
        throws Exception {
        return mockMvc.perform(delete("/trips/" + tripId + "/flights/" + flightNumber)
            .param("username", username));
    }

    private Flight saveFlight(String flightNumber, Instant departure, Instant arrival, int seats) {
        Flight flight = new Flight();
        flight.setFlightNumber(flightNumber);
        flight.setDepartureAirportCode("CLJ");
        flight.setArrivalAirportCode("CDG");
        flight.setDepartureTime(departure);
        flight.setArrivalTime(arrival);
        flight.setNumberOfSeats(seats);
        flight.setPrice(new BigDecimal("150.00"));

        Flight saved = flightRepository.save(flight);
        createdFlightIds.add(saved.getId());
        return saved;
    }

    private static TripDTO tripDto(
        String username, String destination, LocalDate start, LocalDate end
    ) {
        TripDTO dto = new TripDTO();
        dto.setUsername(username);
        dto.setDestination(destination);
        dto.setStartDate(start);
        dto.setEndDate(end);
        return dto;
    }

    /** Flight times are serialised in the server's time zone, so compare instants, not text. */
    private static Instant instantAt(String json, String path) {
        String value = JsonPath.read(json, path);
        return ZonedDateTime.parse(value).toInstant();
    }

    private ResultActions addAccommodation(UUID tripId, String username, UUID accommodationId)
        throws Exception {
        return mockMvc.perform(post("/trips/" + tripId + "/accommodations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new TripAccommodationRequest(username, accommodationId))));
    }

    private ResultActions getAccommodations(UUID tripId, String username) throws Exception {
        return mockMvc.perform(get("/trips/" + tripId + "/accommodations")
            .param("username", username));
    }

    private ResultActions removeAccommodation(UUID tripId, UUID accommodationId, String username)
        throws Exception {
        return mockMvc.perform(delete("/trips/" + tripId + "/accommodations/" + accommodationId)
            .param("username", username));
    }

    private Accommodation saveAccommodation(String name, String address, String city) {
        Accommodation saved = accommodationRepository.save(
            new Accommodation(name, BigDecimal.valueOf(250), address, city));
        createdAccommodationIds.add(saved.getId());
        return saved;
    }

    private void createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.local");
        user.setFirstName("Test");
        user.setLastName("User");
        createdUserIds.add(userRepository.save(user).getId());
    }

    /** Creates a real user with a unique name; the API rejects trips for unknown users. */
    private String user(String prefix) {
        String username = unique(prefix);
        createUser(username);
        return username;
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
