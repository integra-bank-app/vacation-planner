package integra.vacation_planner_backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccommodationResponse(
        UUID id,
        String name,
        BigDecimal pricePerNight
) {
}