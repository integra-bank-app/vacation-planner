package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.TripRestaurant;
import integra.vacation_planner_backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    Page<Trip> findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
        User user,
        LocalDate from,
        LocalDate to,
        Pageable pageable
    );

    @Query("SELECT COUNT(t) FROM Trip t JOIN t.flights f WHERE f.id = :flightId")
    long countTripsUsingFlight(@Param("flightId") UUID flightId);

    @Query("SELECT t FROM Trip t JOIN FETCH t.user WHERE t.id = :tripId")
    Optional<Trip> findWithOwnerById(@Param("tripId") UUID tripId);

    @Query("SELECT a FROM Trip t JOIN t.accommodations a WHERE t.id = :tripId")
    List<Accommodation> findAccommodationsOfTrip(@Param("tripId") UUID tripId);

    @Query("SELECT f FROM Trip t JOIN t.flights f WHERE t.id = :tripId")
    List<Flight> findFlightsOfTrip(@Param("tripId") UUID tripId);

    @Query("SELECT tr FROM TripRestaurant tr JOIN FETCH tr.restaurant WHERE tr.trip.id = :tripId")
    List<TripRestaurant> findVisitsOfTrip(@Param("tripId") UUID tripId);
}
