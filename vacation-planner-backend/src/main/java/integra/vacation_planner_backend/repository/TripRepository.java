package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            String username, LocalDate from, LocalDate to
    );

    @Query("SELECT COUNT(t) FROM Trip t JOIN t.flights f WHERE f.id = :flightId")
    long countTripsUsingFlight(@Param("flightId") UUID flightId);
}
