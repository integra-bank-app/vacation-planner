package integra.vacation_planner_backend.accommodation;

import java.math.BigDecimal;

public class AccommodationRequest {
    /// Mai bine punem datele HTTP intr-un obiect de genul decat direct
    /// in database
    private String name;
    private BigDecimal pricePerNight;
    private String address;
    private String city;

    public AccommodationRequest() {
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }
}