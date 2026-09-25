package integra.vacation_planner_backend.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "flights")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String flightNumber;

    @Column(nullable = false)
    private String departureAirportCode;

    @Column(nullable = false)
    private String arrivalAirportCode;

    @Column(nullable = false)
    private Instant departureTime;
    @Column(nullable = false)
    private Instant arrivalTime;

    @Column(nullable = false)
    private Integer numberOfSeats;
}