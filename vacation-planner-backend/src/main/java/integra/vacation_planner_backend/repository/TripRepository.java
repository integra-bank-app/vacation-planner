package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    Page<Trip> findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
        User user,
        LocalDate from,
        LocalDate to,
        Pageable pageable
    );

    @Query(
        value = """
        SELECT accommodation
        FROM Accommodation accommodation
        WHERE accommodation.id IN (
            SELECT linked.id
            FROM Trip trip
            JOIN trip.accommodations linked
            WHERE trip.id = :tripId
        )
        """,
        countQuery = """
        SELECT COUNT(accommodation)
        FROM Accommodation accommodation
        WHERE accommodation.id IN (
            SELECT linked.id
            FROM Trip trip
            JOIN trip.accommodations linked
            WHERE trip.id = :tripId
        )
        """
    )
    Page<Accommodation> findAccommodationsByTripId(
        @Param("tripId") UUID tripId,
        Pageable pageable
    );
}
