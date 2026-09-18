package integra.vacation_planner_backend.accommodation;

import java.math.BigDecimal;

public class UpdatePriceRequest {

    private BigDecimal pricePerNight;

    public UpdatePriceRequest() {
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}