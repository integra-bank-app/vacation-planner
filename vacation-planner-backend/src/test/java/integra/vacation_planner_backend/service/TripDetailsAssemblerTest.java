package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.TripRestaurant;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.TimelineDayResponse;
import integra.vacation_planner_backend.dto.TripDetailsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TripDetailsAssemblerTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    // UTC+3 in July
    private static final ZoneId BUCHAREST = ZoneId.of("Europe/Bucharest");

    private Trip trip;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setUsername("ana.pop");
        owner.setFirstName("Ana");
        owner.setLastName("Pop");

        trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setDestination("Paris");
        trip.setStartDate(LocalDate.of(2026, 7, 1));
        trip.setEndDate(LocalDate.of(2026, 7, 3));
        trip.setUser(owner);
    }

    @Test
    void tripWithNothingLinked_hasEmptyListsAndAFullTimeline() {
        TripDetailsResponse response = assemble(List.of(), List.of(), List.of(), UTC);

        assertNotNull(response.accommodations());
        assertNotNull(response.flights());
        assertNotNull(response.restaurants());
        assertTrue(response.accommodations().isEmpty());
        assertTrue(response.flights().isEmpty());
        assertTrue(response.restaurants().isEmpty());

        assertEquals(3, response.timeline().size());
        assertEquals(LocalDate.of(2026, 7, 1), response.timeline().get(0).date());
        assertEquals(LocalDate.of(2026, 7, 2), response.timeline().get(1).date());
        assertEquals(LocalDate.of(2026, 7, 3), response.timeline().get(2).date());
        for (TimelineDayResponse day : response.timeline()) {
            assertNotNull(day.flights());
            assertNotNull(day.restaurants());
            assertTrue(day.flights().isEmpty());
            assertTrue(day.restaurants().isEmpty());
        }
    }

    @Test
    void copiesTheTripAndOwnerFields() {
        TripDetailsResponse response = assemble(List.of(), List.of(), List.of(), UTC);

        assertEquals(trip.getId(), response.id());
        assertEquals("Paris", response.destination());
        assertEquals(LocalDate.of(2026, 7, 1), response.startDate());
        assertEquals(LocalDate.of(2026, 7, 3), response.endDate());
        assertEquals("ana.pop", response.owner().username());
        assertEquals("Ana", response.owner().firstName());
        assertEquals("Pop", response.owner().lastName());
    }

    @Test
    void aOneDayTrip_hasExactlyOneTimelineDay() {
        trip.setEndDate(trip.getStartDate());

        TripDetailsResponse response = assemble(List.of(), List.of(), List.of(), UTC);

        assertEquals(1, response.timeline().size());
    }

    @Test
    void flightsAreGroupedByTheDepartureDayInTheGivenTimeZone() {
        // 22:30 UTC on July 2nd is already July 3rd in Bucharest
        Flight flight = flight("RO100", Instant.parse("2026-07-02T22:30:00Z"));

        TripDetailsResponse inUtc = assemble(List.of(), List.of(flight), List.of(), UTC);
        assertEquals(1, inUtc.timeline().get(1).flights().size());
        assertTrue(inUtc.timeline().get(2).flights().isEmpty());

        TripDetailsResponse inBucharest = assemble(List.of(), List.of(flight), List.of(), BUCHAREST);
        assertTrue(inBucharest.timeline().get(1).flights().isEmpty());
        assertEquals(1, inBucharest.timeline().get(2).flights().size());
        assertEquals("RO100", inBucharest.timeline().get(2).flights().get(0).flightNumber());
    }

    @Test
    void flightsOfTheSameDay_areOrderedByDepartureTime() {
        Flight late = flight("LATE", Instant.parse("2026-07-02T15:00:00Z"));
        Flight early = flight("EARLY", Instant.parse("2026-07-02T08:00:00Z"));

        TripDetailsResponse response = assemble(List.of(), List.of(late, early), List.of(), UTC);

        List<String> numbers = response.timeline().get(1).flights().stream()
            .map(flight -> flight.flightNumber())
            .toList();
        assertEquals(List.of("EARLY", "LATE"), numbers);
        assertEquals("EARLY", response.flights().get(0).flightNumber());
    }

    @Test
    void restaurantVisits_areOrderedByTimeInsideTheirDay() {
        TripRestaurant dinner = visit("Dinner place", LocalDate.of(2026, 7, 2), LocalTime.of(19, 30));
        TripRestaurant lunch = visit("Lunch place", LocalDate.of(2026, 7, 2), LocalTime.of(12, 0));
        TripRestaurant otherDay = visit("Other day", LocalDate.of(2026, 7, 3), LocalTime.of(9, 0));

        TripDetailsResponse response = assemble(
            List.of(), List.of(), List.of(dinner, otherDay, lunch), UTC
        );

        List<String> secondDay = response.timeline().get(1).restaurants().stream()
            .map(visit -> visit.restaurantName())
            .toList();
        assertEquals(List.of("Lunch place", "Dinner place"), secondDay);
        assertEquals(1, response.timeline().get(2).restaurants().size());
        assertTrue(response.timeline().get(0).restaurants().isEmpty());
        assertEquals(3, response.restaurants().size());
    }

    @Test
    void accommodationsAreSortedByNameAndKeepTheirFields() {
        Accommodation zeta = new Accommodation(
            UUID.randomUUID(), "Zeta Hotel", new BigDecimal("90.00"), "Street 2", "Paris"
        );
        Accommodation alpha = new Accommodation(
            UUID.randomUUID(), "Alpha Hotel", new BigDecimal("150.00"), "Street 1", "Paris"
        );

        TripDetailsResponse response = assemble(List.of(zeta, alpha), List.of(), List.of(), UTC);

        assertEquals(2, response.accommodations().size());
        assertEquals("Alpha Hotel", response.accommodations().get(0).name());
        assertEquals(new BigDecimal("150.00"), response.accommodations().get(0).pricePerNight());
        assertEquals("Street 1", response.accommodations().get(0).address());
        assertEquals("Paris", response.accommodations().get(0).city());
        assertEquals("Zeta Hotel", response.accommodations().get(1).name());
    }

    @Test
    void aFlightOutsideTheTripDates_isInTheFlightsListButNotInTheTimeline() {
        Flight before = flight("BEFORE", Instant.parse("2026-06-20T10:00:00Z"));

        TripDetailsResponse response = assemble(List.of(), List.of(before), List.of(), UTC);

        assertEquals(1, response.flights().size());
        for (TimelineDayResponse day : response.timeline()) {
            assertTrue(day.flights().isEmpty());
        }
    }

    private TripDetailsResponse assemble(
        List<Accommodation> accommodations,
        List<Flight> flights,
        List<TripRestaurant> visits,
        ZoneId zone
    ) {
        return TripDetailsAssembler.assemble(trip, accommodations, flights, visits, zone);
    }

    private Flight flight(String flightNumber, Instant departure) {
        Flight flight = new Flight();
        flight.setId(UUID.randomUUID());
        flight.setFlightNumber(flightNumber);
        flight.setDepartureAirportCode("CLJ");
        flight.setArrivalAirportCode("CDG");
        flight.setDepartureTime(departure);
        flight.setArrivalTime(departure.plusSeconds(7200));
        flight.setNumberOfSeats(100);
        flight.setPrice(new BigDecimal("100.00"));
        return flight;
    }

    private TripRestaurant visit(String restaurantName, LocalDate date, LocalTime time) {
        Restaurant restaurant = new Restaurant(
            UUID.randomUUID(), restaurantName, "Some street 1", LocalTime.of(9, 0), LocalTime.of(23, 0)
        );
        return new TripRestaurant(trip, restaurant, date, time);
    }
}
