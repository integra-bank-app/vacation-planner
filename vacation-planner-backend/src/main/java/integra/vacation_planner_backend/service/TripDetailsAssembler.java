package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.TripRestaurant;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.dto.TimelineDayResponse;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
import integra.vacation_planner_backend.dto.TripDetailsResponse;
import integra.vacation_planner_backend.dto.TripOwnerResponse;
import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Turns the entities loaded from the database into one TripDetailsResponse.
 * It has no database access, so it is easy to unit test.
 */
public final class TripDetailsAssembler {

    private TripDetailsAssembler() {
    }

    public static TripDetailsResponse assemble(
        Trip trip,
        List<Accommodation> accommodations,
        List<Flight> flights,
        List<TripRestaurant> visits,
        ZoneId zone
    ) {
        List<TripAccommodationResponse> accommodationResponses = accommodations.stream()
            .sorted(Comparator.comparing(Accommodation::getName)
                .thenComparing(Accommodation::getId))
            .map(TripDetailsAssembler::toAccommodationResponse)
            .toList();

        List<FlightResponse> flightResponses = flights.stream()
            .sorted(Comparator.comparing(Flight::getDepartureTime)
                .thenComparing(Flight::getFlightNumber))
            .map(flight -> toFlightResponse(flight, zone))
            .toList();

        List<TripRestaurantResponseDTO> visitResponses = visits.stream()
            .sorted(Comparator.comparing(TripRestaurant::getVisitDate)
                .thenComparing(TripRestaurant::getVisitTime))
            .map(TripDetailsAssembler::toVisitResponse)
            .toList();

        return new TripDetailsResponse(
            trip.getId(),
            trip.getDestination(),
            trip.getStartDate(),
            trip.getEndDate(),
            toOwnerResponse(trip.getUser()),
            accommodationResponses,
            flightResponses,
            visitResponses,
            buildTimeline(trip, flightResponses, visitResponses)
        );
    }

    // One entry for every day of the trip, even the days with nothing planned.
    private static List<TimelineDayResponse> buildTimeline(
        Trip trip,
        List<FlightResponse> flights,
        List<TripRestaurantResponseDTO> visits
    ) {
        // The lists are already sorted by time, and groupingBy keeps that order.
        // departureTime() is already in the system time zone, so toLocalDate()
        // gives the day the flight departs in that zone.
        Map<LocalDate, List<FlightResponse>> flightsByDay = flights.stream()
            .collect(Collectors.groupingBy(
                flight -> flight.departureTime().toLocalDate()
            ));

        Map<LocalDate, List<TripRestaurantResponseDTO>> visitsByDay = visits.stream()
            .collect(Collectors.groupingBy(TripRestaurantResponseDTO::visitDate));

        List<TimelineDayResponse> timeline = new ArrayList<>();
        LocalDate day = trip.getStartDate();

        while (!day.isAfter(trip.getEndDate())) {
            timeline.add(new TimelineDayResponse(
                day,
                flightsByDay.getOrDefault(day, List.of()),
                visitsByDay.getOrDefault(day, List.of())
            ));
            day = day.plusDays(1);
        }

        return timeline;
    }

    private static TripOwnerResponse toOwnerResponse(User user) {
        return new TripOwnerResponse(
            user.getUsername(),
            user.getFirstName(),
            user.getLastName()
        );
    }

    private static TripAccommodationResponse toAccommodationResponse(
        Accommodation accommodation
    ) {
        return new TripAccommodationResponse(
            accommodation.getId(),
            accommodation.getName(),
            accommodation.getAddress(),
            accommodation.getCity(),
            accommodation.getPricePerNight()
        );
    }

    private static FlightResponse toFlightResponse(Flight flight, ZoneId zone) {
        return new FlightResponse(
            flight.getFlightNumber(),
            flight.getDepartureAirportCode(),
            flight.getArrivalAirportCode(),
            flight.getDepartureTime().atZone(zone),
            flight.getArrivalTime().atZone(zone),
            flight.getPrice()
        );
    }

    private static TripRestaurantResponseDTO toVisitResponse(TripRestaurant visit) {
        return new TripRestaurantResponseDTO(
            visit.getId(),
            visit.getRestaurant().getId(),
            visit.getRestaurant().getName(),
            visit.getRestaurant().getAddress(),
            visit.getVisitDate(),
            visit.getVisitTime()
        );
    }
}
