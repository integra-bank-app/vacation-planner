package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.dto.AccommodationRequest;
import integra.vacation_planner_backend.dto.AccommodationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccommodationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trips", ignore = true)
    Accommodation toAccommodation(AccommodationRequest request);

    AccommodationResponse toResponse(Accommodation accommodation);
}
