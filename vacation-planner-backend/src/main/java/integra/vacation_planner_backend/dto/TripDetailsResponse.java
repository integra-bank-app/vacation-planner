package integra.vacation_planner_backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TripDetailsResponse(
    UUID id,
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    TripOwnerResponse owner,
    List<TripAccommodationResponse> accommodations,
    List<FlightResponse> flights,
    List<TripRestaurantResponseDTO> restaurants,
    List<TimelineDayResponse> timeline
) { }
