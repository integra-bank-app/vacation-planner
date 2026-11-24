package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.dto.FlightResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FlightPageResponseMapper extends PageResponseMapper<FlightResponse> {
}
