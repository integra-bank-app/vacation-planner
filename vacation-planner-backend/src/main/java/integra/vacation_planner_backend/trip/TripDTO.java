package integra.vacation_planner_backend.trip;

import lombok.Getter;
import lombok.Setter;



import java.time.LocalDate;

@Setter
@Getter
public class TripDTO {
    private String username;

    private String destination;

    private LocalDate startDate;
    private LocalDate endDate;

}