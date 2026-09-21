package integra.vacation_planner_backend.dto;
import java.time.Instant;

public class FlightRequest {

// post/put-> backend
    private String flightNumber;
    private String departureAirportCode;
    private String arrivalAirportCode;
    private Instant departureTime;
    private Instant arrivalTime;
    private Integer numberOfSeats;

    public String getFlightNumber() {
        return flightNumber;
    }
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getDepartureAirportCode() {
        return departureAirportCode;
    }
    public void setDepartureAirportCode(String departureAirportCode) {this.departureAirportCode = departureAirportCode;}

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

    public Integer getNumberOfSeats() {return numberOfSeats;}
    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }
}