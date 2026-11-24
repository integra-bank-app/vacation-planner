package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.TripRestaurant;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
import integra.vacation_planner_backend.dto.TripResponse;
import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripMapper {
    @Mapping(target = "username", source = "user.username")
    TripResponse toResponse(Trip trip);

    TripAccommodationResponse toAccommodationResponse(Accommodation accommodation);

    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "restaurantAddress", source = "restaurant.address")
    TripRestaurantResponseDTO toResturantResponse(TripRestaurant tripRestaurant);
}
