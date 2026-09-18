package integra.vacation_planner_backend.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "flights")  //belongs to the table created in Flyway
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String flightNumber;

    @Column(nullable = false)
    private String departureAirportCode;

    @Column(nullable = false)
    private String arrivalAirportCode;

    @Column(nullable = false)
    private Instant departureTime; //for UTC timing

    @Column(nullable = false)
    private Instant arrivalTime;

    @Column(nullable = false)
    private Integer numberOfSeats;

    public Flight() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getDepartureAirportCode() {
        return departureAirportCode;
    }
    public void setDepartureAirportCode(String departureAirportCode) {
        this.departureAirportCode = departureAirportCode;}

    public String getArrivalAirportCode() {
        return arrivalAirportCode;
    }
    public void setArrivalAirportCode(String arrivalAirportCode) {
        this.arrivalAirportCode = arrivalAirportCode;
    }

    public Instant getDepartureTime() {
        return departureTime;
    }
    public void setDepartureTime(Instant departureTime) {
        this.departureTime = departureTime;
    }

    public Instant getArrivalTime() {
        return arrivalTime;
    }
    public void setArrivalTime(Instant arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }
    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }
}