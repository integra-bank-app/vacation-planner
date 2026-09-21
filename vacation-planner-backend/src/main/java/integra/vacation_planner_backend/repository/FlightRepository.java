package integra.vacation_planner_backend.repository;
import integra.vacation_planner_backend.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber); //may not exist
    List<Flight> findByDepartureAirportCodeOrArrivalAirportCode(String departureAirportCode, String arrivalAirportCode);//from/to
}