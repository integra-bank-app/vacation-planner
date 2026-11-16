package integra.vacation_planner_backend.dto;

import java.time.LocalDate;
import java.util.List;

public record TimelineDayResponse(
    LocalDate date,
    List<FlightResponse> flights,
    List<TripRestaurantResponseDTO> restaurants
) { }
