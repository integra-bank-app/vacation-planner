package integra.vacation_planner_backend.repository;
import integra.vacation_planner_backend.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
//access to the db
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber); //poate sau nu sa existe
    List<Flight> findByDepartureAirportCodeOrArrivalAirportCode(String departureAirportCode, String arrivalAirportCode);
}