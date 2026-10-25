package integra.vacation_planner_backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TripRestaurantResponseDTO(
    UUID id,
    UUID restaurantId,
    String restaurantName,
    String restaurantAddress,
    LocalDate visitDate,
    LocalTime visitTime
) { }
