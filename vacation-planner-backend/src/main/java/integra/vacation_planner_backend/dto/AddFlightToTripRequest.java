package integra.vacation_planner_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFlightToTripRequest {
    private String username;

    private String flightNumber;
}
