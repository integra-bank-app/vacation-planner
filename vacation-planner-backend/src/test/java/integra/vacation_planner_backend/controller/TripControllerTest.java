package integra.vacation_planner_backend.controller;

import com.jayway.jsonpath.JsonPath;
import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.dto.AddFlightToTripRequest;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.repository.FlightRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import org.junit.jupiter.api.AfterEach;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private FlightRepository flightRepository;

    private final List<UUID> createdTripIds = new ArrayList<>();

    private final List<UUID> createdFlightIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        // trips first: they reference flights through the trip_flight join table
        tripRepository.deleteAllById(createdTripIds);
        flightRepository.deleteAllById(createdFlightIds);
        createdTripIds.clear();
        createdFlightIds.clear();
    }

    // ------------------------------------------------------------------
    // Full flow
    // ------------------------------------------------------------------

    @Test
    void fullFlow_createAddFlightsUpdateRemoveFlightAndDeleteTrip() throws Exception {
        String user = unique("alice");
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
    void createTrip_persistsTrip_andAcceptsASingleDayTrip() throws Exception {
        String user = unique("bob");

        UUID tripId = createTrip(user, "Rome", START, END);
        UUID oneDay = createTrip(user, "Rome", START, START);

        assertThat(tripRepository.findById(tripId)).hasValueSatisfying(trip -> {
            assertThat(trip.getUsername()).isEqualTo(user);
            assertThat(trip.getDestination()).isEqualTo("Rome");
            assertThat(trip.getStartDate()).isEqualTo(START);
            assertThat(trip.getEndDate()).isEqualTo(END);
        });
        assertThat(oneDay).isNotEqualTo(tripId);
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
            Arguments.of("bob", null, START, END, "destination is required"),
            Arguments.of("bob", " ", START, END, "destination is required"),
            Arguments.of("bob", "Paris", null, END, "startDate and endDate are required"),
            Arguments.of("bob", "Paris", START, null, "startDate and endDate are required"),
            Arguments.of("bob", "Paris", END, START, DATE_ORDER_ERROR)
        );
    }

    // ------------------------------------------------------------------
    // GET /trips
    // ------------------------------------------------------------------

    @Test
    void getTrips_returnsOnlyTheUsersTripsFullyInsideTheRange() throws Exception {
        String user = unique("carol");
        createTrip(user, "Paris", LocalDate.of(2027, 6, 10), LocalDate.of(2027, 6, 15));
        createTrip(user, "Rome", LocalDate.of(2027, 7, 1), LocalDate.of(2027, 7, 5));
        createTrip(user, "Oslo", LocalDate.of(2027, 8, 10), LocalDate.of(2027, 8, 20));
        createTrip(unique("dave"), "Madrid", LocalDate.of(2027, 6, 10), LocalDate.of(2027, 6, 15));

        searchTrips(user, "2027-06-01", "2027-07-31")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[?(@.destination == 'Paris')]").exists())
            .andExpect(jsonPath("$[?(@.destination == 'Rome')]").exists());

        // exact boundaries are inclusive
        searchTrips(user, "2027-06-10", "2027-06-15")
            .andExpect(jsonPath("$.length()").value(1));

        // a trip only partly inside the range is excluded (containment, not overlap)
        searchTrips(user, "2027-06-12", "2027-06-30").andExpect(content().json("[]"));
        searchTrips(user, "2027-06-01", "2027-06-12").andExpect(content().json("[]"));
    }

    @Test
    void getTrips_doesNotExposeTheFlightsCollection() throws Exception {
        String user = unique("carol");
        UUID tripId = createTrip(user, "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);
        addFlight(tripId, user, flight.getFlightNumber()).andExpect(status().isNoContent());

        searchTrips(user, "2027-06-01", "2027-06-30")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(tripId.toString()))
            .andExpect(jsonPath("$[0].flights").doesNotExist());
    }

    @Test
    void getTrips_rejectsAReversedRangeAndABlankUsername() throws Exception {
        searchTrips("bob", "2027-06-30", "2027-06-01")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(DATE_ORDER_ERROR));

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
        String user = unique("erin");
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
        String user = unique("erin");
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
        String user = unique("erin");
        UUID tripId = createTrip(user, "Paris", START, END);

        putTrip(tripId, tripDto(user, "Rome", null, LocalDate.of(2027, 6, 20)))
            .andExpect(status().isOk());

        assertThat(tripRepository.findById(tripId))
            .hasValueSatisfying(trip -> assertThat(trip.getDestination()).isEqualTo("Paris"));
    }

    @Test
    void updateTrip_returnsForbiddenForANonOwner_andNotFoundForAnUnknownTrip() throws Exception {
        UUID tripId = createTrip(unique("erin"), "Paris", START, END);
        TripDTO update = tripDto(unique("mallory"), null, LocalDate.of(2027, 6, 12), null);

        putTrip(tripId, update)
            .andExpect(status().isForbidden())
            .andExpect(content().string("Not allowed to update this trip"));
        assertThat(tripRepository.findById(tripId))
            .hasValueSatisfying(trip -> assertThat(trip.getStartDate()).isEqualTo(START));

        putTrip(UUID.randomUUID(), update)
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void updateTrip_rejectsAMissingUsername(String username) throws Exception {
        UUID tripId = createTrip(unique("erin"), "Paris", START, END);

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
        String first = unique("erin");
        String second = unique("frank");
        UUID firstTrip = createTrip(first, "Paris", START, END);
        UUID secondTrip = createTrip(second, "Paris", START, END);
        addFlight(firstTrip, first, flight.getFlightNumber()).andExpect(status().isNoContent());
        addFlight(secondTrip, second, flight.getFlightNumber()).andExpect(status().isConflict());

        deleteTrip(firstTrip, first).andExpect(status().isOk());

        assertThat(flightRepository.findByFlightNumber(flight.getFlightNumber())).isPresent();
        addFlight(secondTrip, second, flight.getFlightNumber()).andExpect(status().isNoContent());
    }

    @Test
    void deleteTrip_returnsForbiddenForANonOwner_notFoundWhenUnknown_badRequestWhenBlank()
        throws Exception {
        String owner = unique("erin");
        UUID tripId = createTrip(owner, "Paris", START, END);

        deleteTrip(tripId, unique("mallory"))
            .andExpect(status().isForbidden())
            .andExpect(content().string("Not allowed to delete this trip"));

        deleteTrip(tripId, " ")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("username is required"));

        assertThat(tripRepository.existsById(tripId)).isTrue();

        deleteTrip(UUID.randomUUID(), owner)
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));
    }

    // ------------------------------------------------------------------
    // POST /trips/{id}/flights
    // ------------------------------------------------------------------

    @Test
    void addFlight_allowsTheSameFlightOnDifferentTrips_butNotTwiceOnOneTrip() throws Exception {
        String user = unique("frank");
        UUID firstTrip = createTrip(user, "Paris", START, END);
        UUID secondTrip = createTrip(user, "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);

        addFlight(firstTrip, user, flight.getFlightNumber()).andExpect(status().isNoContent());
        addFlight(secondTrip, user, flight.getFlightNumber()).andExpect(status().isNoContent());
        addFlight(firstTrip, user, flight.getFlightNumber()).andExpect(status().isConflict());

        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isEqualTo(2);
    }

    @Test
    void addFlight_fillsTheFlightToCapacity_thenReturnsConflict() throws Exception {
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 2);
        List<String> users = List.of(unique("u1"), unique("u2"), unique("u3"));
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
    void addFlight_returnsForbiddenForANonOwner_andNotFoundForUnknownTripOrFlight()
        throws Exception {
        String owner = unique("frank");
        UUID tripId = createTrip(owner, "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);

        addFlight(tripId, unique("mallory"), flight.getFlightNumber())
            .andExpect(status().isForbidden())
            .andExpect(content().string("Not allowed to add flights to this trip"));
        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isZero();

        addFlight(UUID.randomUUID(), owner, flight.getFlightNumber())
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));

        addFlight(tripId, owner, unique("NOPE")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addFlight_rejectsAMissingFlightNumber(String flightNumber) throws Exception {
        String user = unique("frank");
        UUID tripId = createTrip(user, "Paris", START, END);

        addFlight(tripId, user, flightNumber)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("flightNumber is required"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void addFlight_rejectsAMissingUsername(String username) throws Exception {
        UUID tripId = createTrip(unique("frank"), "Paris", START, END);
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
            String user = unique("racer");
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
        String user = unique("ivan");
        UUID tripId = createTrip(user, "Paris", START, END);

        getFlights(tripId, user)
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void getFlights_returnsForbiddenNotFoundAndBadRequestForTheUsualMistakes() throws Exception {
        String owner = unique("ivan");
        UUID tripId = createTrip(owner, "Paris", START, END);

        getFlights(tripId, unique("mallory"))
            .andExpect(status().isForbidden())
            .andExpect(content().string("User is not authorized to view the flights for this trip"));

        getFlights(UUID.randomUUID(), owner)
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));

        getFlights(tripId, " ")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Username is required"));
    }

    // ------------------------------------------------------------------
    // DELETE /trips/{id}/flights/{flightNumber}
    // ------------------------------------------------------------------

    @Test
    void removeFlight_unlinksOnlyThatFlightFromOnlyThatTrip_andFreesTheSeat() throws Exception {
        String user = unique("judy");
        String other = unique("karl");
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

        // removing it a second time is a 404
        removeFlight(tripId, shared.getFlightNumber(), user).andExpect(status().isNotFound());
    }

    @Test
    void removeFlight_returnsForbiddenForANonOwner_andNotFoundForUnknownThings() throws Exception {
        String owner = unique("judy");
        UUID tripId = createTrip(owner, "Paris", START, END);
        Flight flight = saveFlight(unique("FL"), OUT_DEP, OUT_ARR, 5);
        Flight notOnTrip = saveFlight(unique("FL"), IN_DEP, IN_ARR, 5);
        addFlight(tripId, owner, flight.getFlightNumber()).andExpect(status().isNoContent());

        removeFlight(tripId, flight.getFlightNumber(), unique("mallory"))
            .andExpect(status().isForbidden())
            .andExpect(content().string("User is not authorized to remove flights from this trip"));
        assertThat(tripRepository.countTripsUsingFlight(flight.getId())).isEqualTo(1);

        removeFlight(UUID.randomUUID(), flight.getFlightNumber(), owner)
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));

        removeFlight(tripId, unique("NOPE"), owner).andExpect(status().isNotFound());
        removeFlight(tripId, notOnTrip.getFlightNumber(), owner).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Requests Spring rejects before the service is reached
    // ------------------------------------------------------------------

    @Test
    void malformedRequests_areAllRejectedWithBadRequest() throws Exception {
        MediaType json = MediaType.APPLICATION_JSON;
        List<RequestBuilder> requests = List.of(
            post("/trips").contentType(json).content("{ this is not json"),
            post("/trips").contentType(json),
            post("/trips").contentType(json).content(
                "{\"username\":\"bob\",\"destination\":\"Paris\",\"startDate\":\"2027-13-45\","
                    + "\"endDate\":\"2027-06-15\"}"),
            put("/trips/not-a-uuid").contentType(json).content("{}"),
            post("/trips/not-a-uuid/flights").contentType(json).content("{}"),
            get("/trips/not-a-uuid/flights").param("username", "bob"),
            delete("/trips/not-a-uuid").param("username", "bob"),
            delete("/trips/not-a-uuid/flights/RO101").param("username", "bob"),
            delete("/trips/" + UUID.randomUUID()),
            get("/trips/" + UUID.randomUUID() + "/flights"),
            get("/trips").param("startDate", "2027-06-01").param("endDate", "2027-06-30"),
            get("/trips").param("username", "bob").param("startDate", "not-a-date")
                .param("endDate", "2027-06-30")
        );

        for (RequestBuilder request : requests) {
            mockMvc.perform(request).andExpect(status().isBadRequest());
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

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

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
