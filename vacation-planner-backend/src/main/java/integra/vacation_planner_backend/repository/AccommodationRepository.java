package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccommodationRepository
        extends JpaRepository<Accommodation, UUID> {

    List<Accommodation> findByCityIgnoreCaseOrderByPricePerNightAsc(String city);
}