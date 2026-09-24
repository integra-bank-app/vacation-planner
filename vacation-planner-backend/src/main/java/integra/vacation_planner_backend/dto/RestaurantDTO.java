package integra.vacation_planner_backend.dto;

import java.time.LocalTime;
import java.util.UUID;

public record RestaurantDTO (UUID id, String name, String address, LocalTime openingHour, LocalTime closingHour, Double rating) {}
