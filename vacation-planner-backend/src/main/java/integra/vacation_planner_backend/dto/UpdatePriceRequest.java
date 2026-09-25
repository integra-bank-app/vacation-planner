package integra.vacation_planner_backend.dto;

import java.math.BigDecimal;

public record UpdatePriceRequest(
        BigDecimal pricePerNight
) {
}