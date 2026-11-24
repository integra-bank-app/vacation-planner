package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.dto.RestaurantDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {
    @Mapping(target = "rating", source = "id")
    RestaurantDTO toDTO(Restaurant restaurant);

    default double idToRating(UUID id) {
        return 5.0;
    }

}
