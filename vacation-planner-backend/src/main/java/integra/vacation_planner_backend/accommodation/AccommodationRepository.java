package integra.vacation_planner_backend.accommodation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccommodationRepository
        extends JpaRepository<Accommodation, Long> {

    List<Accommodation> findByCityIgnoreCaseOrderByPricePerNightAsc(String city);
}