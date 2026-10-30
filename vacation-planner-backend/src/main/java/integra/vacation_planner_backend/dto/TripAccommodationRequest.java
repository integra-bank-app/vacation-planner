package integra.vacation_planner_backend.dto;

import java.util.UUID;

public record TripAccommodationRequest(
    String username,
    UUID accommodationId
) {
}
