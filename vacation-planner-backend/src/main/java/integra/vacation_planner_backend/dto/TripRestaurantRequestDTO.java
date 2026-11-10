package integra.vacation_planner_backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TripRestaurantRequestDTO(
    String username,
    UUID restaurantId,
    LocalDate visitDate,
    LocalTime visitTime
) { }
