package integra.vacation_planner_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accommodation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "price_per_night")
    private BigDecimal pricePerNight;

    private String address;

    private String city;

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
}