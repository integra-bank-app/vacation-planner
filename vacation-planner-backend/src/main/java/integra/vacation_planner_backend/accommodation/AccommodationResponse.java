package integra.vacation_planner_backend.accommodation;

import java.math.BigDecimal;

public class AccommodationResponse {
    /// Contine doar id,name si pricePerNight pentru ca asta cere ticketul
    /// sa returneze GET
    private Long id;
    private String name;
    private BigDecimal pricePerNight;

    public AccommodationResponse(
            Long id,
            String name,
            BigDecimal pricePerNight
    ) {
        this.id = id;
        this.name = name;
        this.pricePerNight = pricePerNight;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
}