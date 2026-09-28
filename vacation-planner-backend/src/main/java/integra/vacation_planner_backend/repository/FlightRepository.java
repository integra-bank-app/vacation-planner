package integra.vacation_planner_backend.repository;
import integra.vacation_planner_backend.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlightRepository extends JpaRepository<Flight, UUID> {
    Optional<Flight> findByFlightNumber(String flightNumber);
    List<Flight> findByDepartureAirportCodeOrArrivalAirportCode(String departureAirportCode, String arrivalAirportCode);
}