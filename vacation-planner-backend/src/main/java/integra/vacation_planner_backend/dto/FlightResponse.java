package integra.vacation_planner_backend.dto;
import java.time.ZonedDateTime;

public record FlightResponse(
        String flightNumber,
        String departureAirport,
        String arrivalAirport,
        ZonedDateTime departureTime,
        ZonedDateTime arrivalTime
) {
}