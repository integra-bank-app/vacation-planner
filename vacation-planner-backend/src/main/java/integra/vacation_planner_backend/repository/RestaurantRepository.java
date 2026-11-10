package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.domain.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    Page<Restaurant> getRestaurantsByAddressLike(
        String query,
        Pageable pageable
    );
}
