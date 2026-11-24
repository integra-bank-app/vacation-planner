package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripRestaurantPageResponseMapper extends PageResponseMapper<TripRestaurantResponseDTO> {
}
