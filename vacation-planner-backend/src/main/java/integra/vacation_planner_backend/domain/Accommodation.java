package integra.vacation_planner_backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;

import java.util.HashSet;
import java.util.Set;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "accommodation")
@Getter
@Setter
@NoArgsConstructor
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "price_per_night")
    private BigDecimal pricePerNight;

    private String address;

    private String city;

    @ManyToMany(mappedBy = "accommodations", fetch = FetchType.LAZY)
    private Set<Trip> trips = new HashSet<>();

    public Accommodation(
        UUID id,
        String name,
        BigDecimal pricePerNight,
        String address,
        String city
    ) {
        this.id = id;
        this.name = name;
        this.pricePerNight = pricePerNight;
        this.address = address;
        this.city = city;
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
}
