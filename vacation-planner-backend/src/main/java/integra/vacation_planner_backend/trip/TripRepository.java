package integra.vacation_planner_backend.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip,Long> {
    List<Trip> findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
            String username, LocalDate from, LocalDate to
    );
}