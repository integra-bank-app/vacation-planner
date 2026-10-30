package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.domain.Flight;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlightRepository extends JpaRepository<Flight, UUID> {
    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findByDepartureAirportCodeOrArrivalAirportCode(String departureAirportCode, String arrivalAirportCode);

    List<Flight> findByDepartureTimeAfter(Instant time);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.flightNumber = :flightNumber")
    Optional<Flight> findByFlightNumberForUpdate(
        @Param("flightNumber") String flightNumber
    );
}
