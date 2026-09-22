package integra.vacation_planner_backend.service;
import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.exception.ValidationException;
import integra.vacation_planner_backend.model.Flight;
import integra.vacation_planner_backend.repository.FlightRepository;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public Flight createFlight(FlightRequest request) {
        validateFlight(request);

        if (flightRepository.findByFlightNumber(request.flightNumber()).isPresent()) {
            throw new ConflictException("Flight already exists");
        }

        Flight flight = new Flight();
        flight.setFlightNumber(request.flightNumber());
        flight.setDepartureAirportCode(request.departureAirportCode());
        flight.setArrivalAirportCode(request.arrivalAirportCode());
        flight.setDepartureTime(request.departureTime());
        flight.setArrivalTime(request.arrivalTime());
        flight.setNumberOfSeats(request.numberOfSeats());

        return flightRepository.save(flight);
    }

    public List<FlightResponse> getFlights(String airportCode) {
        if (airportCode == null || airportCode.isBlank()) {
            throw new ValidationException("Airport code is required");
        }

        List<Flight> flights = flightRepository.findByDepartureAirportCodeOrArrivalAirportCode(
                airportCode,
                airportCode
        );

        return flights.stream()
                .map(this::toResponse)
                .toList();
    }

    private FlightResponse toResponse(Flight flight) {
        return new FlightResponse(
                flight.getFlightNumber(),
                flight.getDepartureAirportCode(),
                flight.getArrivalAirportCode(),
                flight.getDepartureTime().atZone(ZoneId.systemDefault()),
                flight.getArrivalTime().atZone(ZoneId.systemDefault())
        );
    }

    public Flight updateFlight(FlightRequest request) {
        validateFlight(request);

        Flight flight = flightRepository.findByFlightNumber(request.flightNumber())
                .orElseThrow(() -> new NotFoundException("Flight not found"));

        flight.setDepartureAirportCode(request.departureAirportCode());
        flight.setArrivalAirportCode(request.arrivalAirportCode());
        flight.setDepartureTime(request.departureTime());
        flight.setArrivalTime(request.arrivalTime());
        flight.setNumberOfSeats(request.numberOfSeats());

        return flightRepository.save(flight);
    }

    public void deleteFlight(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new ValidationException("Flight number is required");
        }

        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new NotFoundException("Flight not found"));

        flightRepository.delete(flight);
    }

    private void validateFlight(FlightRequest request) {
        if (request.flightNumber() == null || request.flightNumber().isBlank()) {
            throw new ValidationException("Flight number is required");
        }

        if (request.departureAirportCode() == null || request.departureAirportCode().isBlank()) {
            throw new ValidationException("Departure airport is required");
        }

        if (request.arrivalAirportCode() == null || request.arrivalAirportCode().isBlank()) {
            throw new ValidationException("Arrival airport is required");
        }

        if (request.departureTime() == null || request.arrivalTime() == null) {
            throw new ValidationException("Departure and arrival time are required");
        }

        if (!request.arrivalTime().isAfter(request.departureTime())) {
            throw new ValidationException("Arrival time must be after departure time");
        }

        if (request.numberOfSeats() == null || request.numberOfSeats() <= 0) {
            throw new ValidationException("Number of seats must be greater than 0");
        }
    }
}