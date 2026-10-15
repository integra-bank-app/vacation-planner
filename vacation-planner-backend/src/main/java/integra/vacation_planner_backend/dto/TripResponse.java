package integra.vacation_planner_backend.dto;

import java.time.LocalDate;
import java.util.UUID;

public record TripResponse(
    UUID id,
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    String username
) {
}
