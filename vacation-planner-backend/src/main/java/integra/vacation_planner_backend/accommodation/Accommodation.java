package integra.vacation_planner_backend.accommodation;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accommodation")
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "price_per_night")
    private BigDecimal pricePerNight;

    private String address;

    private String city;

    public Accommodation() {
    }

    public Accommodation(
            String name,
            BigDecimal pricePerNight,
            String address,
            String city
    ) {
        this.name = name;
        this.pricePerNight = pricePerNight;
        this.address = address;
        this.city = city;
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
