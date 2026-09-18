package integra.vacation_planner_backend.service;
import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
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

    //post
    public Flight createFlight(FlightRequest request) {
        Flight flight = new Flight();
        flight.setFlightNumber(request.getFlightNumber());
        flight.setDepartureAirportCode(request.getDepartureAirportCode());
        flight.setArrivalAirportCode(request.getArrivalAirportCode());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setNumberOfSeats(request.getNumberOfSeats());
        return flightRepository.save(flight);
    }

    //get
    public List<FlightResponse> getFlights(String airportCode) {

        List<Flight> flights = flightRepository.findByDepartureAirportCodeOrArrivalAirportCode(airportCode, airportCode);
        return flights.stream().map(this::toResponse).toList();
    }

    //transforma Flight in raspunsul cerut de get
    private FlightResponse toResponse(Flight flight) {

        FlightResponse response = new FlightResponse();
        response.setFlightNumber(flight.getFlightNumber());
        response.setDepartureAirport(flight.getDepartureAirportCode());
        response.setArrivalAirport(flight.getArrivalAirportCode());
        // UTC
        response.setDepartureTime(flight.getDepartureTime().atZone(ZoneId.systemDefault()));
        response.setArrivalTime(flight.getArrivalTime().atZone(ZoneId.systemDefault()));
        return response;
    }

    //put
    public Flight updateFlight(FlightRequest request) {

        Flight flight = flightRepository.findByFlightNumber(request.getFlightNumber()).orElseThrow(() -> new RuntimeException("Flight not found"));
        flight.setDepartureAirportCode(request.getDepartureAirportCode());
        flight.setArrivalAirportCode(request.getArrivalAirportCode());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setNumberOfSeats(request.getNumberOfSeats());
        return flightRepository.save(flight);
    }


    //delete
    public void deleteFlight(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber).orElseThrow(() -> new RuntimeException("Flight not found"));
        flightRepository.delete(flight);
    }
}