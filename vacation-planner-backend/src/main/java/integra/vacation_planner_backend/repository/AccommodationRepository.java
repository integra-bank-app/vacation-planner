package integra.vacation_planner_backend.repository;

import integra.vacation_planner_backend.domain.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccommodationRepository
    extends JpaRepository<Accommodation, UUID> {

    Page<Accommodation> findByCityIgnoreCase(
        String city,
        Pageable pageable
    );
}
