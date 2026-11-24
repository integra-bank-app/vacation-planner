package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.dto.RestaurantDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestaurantPageResponseMapper extends PageResponseMapper<RestaurantDTO> {
}
