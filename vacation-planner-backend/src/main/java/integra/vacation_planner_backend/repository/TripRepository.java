package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
        User user, LocalDate from, LocalDate to
    );
}
