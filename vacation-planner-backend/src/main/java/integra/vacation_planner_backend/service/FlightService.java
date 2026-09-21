package integra.vacation_planner_backend.service;
import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.model.Flight;
import integra.vacation_planner_backend.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.ZoneId;
import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    public FlightService(FlightRepository flightRepository) {this.flightRepository = flightRepository;}


    public Flight createFlight(FlightRequest request) {

        validateFlight(request);
        if (flightRepository.findByFlightNumber(request.getFlightNumber()).isPresent()) {throw new ResponseStatusException(HttpStatus.CONFLICT, "Flight already exists");}

        Flight flight = new Flight();
        flight.setFlightNumber(request.getFlightNumber());
        flight.setDepartureAirportCode(request.getDepartureAirportCode());
        flight.setArrivalAirportCode(request.getArrivalAirportCode());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setNumberOfSeats(request.getNumberOfSeats());
        return flightRepository.save(flight);
    }

    public List<FlightResponse> getFlights(String airportCode) {

        if (airportCode == null || airportCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Airport code is required");
        }

        List<Flight> flights = flightRepository.findByDepartureAirportCodeOrArrivalAirportCode(airportCode, airportCode);
        return flights.stream().map(this::toResponse).toList();
    }


    private FlightResponse toResponse(Flight flight) {

        FlightResponse response = new FlightResponse();
        response.setFlightNumber(flight.getFlightNumber());
        response.setDepartureAirport(flight.getDepartureAirportCode());
        response.setArrivalAirport(flight.getArrivalAirportCode());
        //  convert UTC to local time zone
        response.setDepartureTime(flight.getDepartureTime().atZone(ZoneId.systemDefault()));
        response.setArrivalTime(flight.getArrivalTime().atZone(ZoneId.systemDefault()));
        return response;
    }


    public Flight updateFlight(FlightRequest request) {

        validateFlight(request);
        Flight flight = flightRepository.findByFlightNumber(request.getFlightNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found"));

        flight.setDepartureAirportCode(request.getDepartureAirportCode());
        flight.setArrivalAirportCode(request.getArrivalAirportCode());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setNumberOfSeats(request.getNumberOfSeats());
        return flightRepository.save(flight);
    }


    //delete
    public void deleteFlight(String flightNumber) {

        if (flightNumber == null || flightNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flight number is required");
        }

        Flight flight = flightRepository.findByFlightNumber(flightNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found"));
        flightRepository.delete(flight);
    }

    private void validateFlight(FlightRequest request) {

        if (request.getFlightNumber() == null || request.getFlightNumber().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flight number is required");
        }

        if (request.getDepartureAirportCode() == null || request.getDepartureAirportCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure airport is required");
        }

        if (request.getArrivalAirportCode() == null || request.getArrivalAirportCode().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arrival airport is required");
        }

        if (request.getDepartureTime() == null || request.getArrivalTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure and arrival time are required");
        }

        if (!request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arrival time must be after departure time");
        }

        if (request.getNumberOfSeats() == null || request.getNumberOfSeats() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of seats must be greater than 0");
        }
    }
}