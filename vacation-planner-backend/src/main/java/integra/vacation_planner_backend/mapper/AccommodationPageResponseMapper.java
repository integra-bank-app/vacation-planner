package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.dto.AccommodationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccommodationPageResponseMapper extends PageResponseMapper<AccommodationResponse> {
}
