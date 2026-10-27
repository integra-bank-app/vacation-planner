package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.domain.TripRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface TripRestaurantRepository
extends JpaRepository<TripRestaurant, UUID>  {
    List<TripRestaurant> findByTrip_IdOrderByVisitDateAscVisitTimeAsc(UUID tripId);

    boolean existsByTrip_IdAndRestaurant_IdAndVisitDateAndVisitTime(
        UUID tripId,
        UUID restaurantId,
        LocalDate visitDate,
        LocalTime visitTime
    );

}
