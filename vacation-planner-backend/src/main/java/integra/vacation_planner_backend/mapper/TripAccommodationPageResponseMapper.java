package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.dto.TripAccommodationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripAccommodationPageResponseMapper extends PageResponseMapper<TripAccommodationResponse> {
}
