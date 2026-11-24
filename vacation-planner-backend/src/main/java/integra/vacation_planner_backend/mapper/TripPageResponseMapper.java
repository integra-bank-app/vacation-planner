package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.dto.TripResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripPageResponseMapper extends PageResponseMapper<TripResponse> {
}
