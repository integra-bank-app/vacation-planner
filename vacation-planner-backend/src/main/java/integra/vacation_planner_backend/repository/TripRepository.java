package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip,Long> {
    List<Trip> findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            String username, LocalDate from, LocalDate to
    );
}