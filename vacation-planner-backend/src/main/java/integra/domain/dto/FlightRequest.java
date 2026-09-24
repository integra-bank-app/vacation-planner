package integra.domain.dto;
import java.time.Instant;

public record FlightRequest(
        String flightNumber,
        String departureAirportCode,
        String arrivalAirportCode,
        Instant departureTime,
        Instant arrivalTime,
        Integer numberOfSeats
) {
}