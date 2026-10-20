package integra.vacation_planner_backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TripAccommodationResponse(
    UUID id,
    String name,
    String address,
    String city,
    BigDecimal pricePerNight
) {
}
