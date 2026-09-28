package integra.vacation_planner_backend.dto;

import java.math.BigDecimal;

public record AccommodationRequest(
        String name,
        BigDecimal pricePerNight,
        String address,
        String city
) {
}