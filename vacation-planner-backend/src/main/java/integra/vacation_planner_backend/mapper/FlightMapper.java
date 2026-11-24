package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Mapper(componentModel = "spring")
public interface FlightMapper {
    @Mapping(target = "id", ignore = true)
    Flight toFlight(FlightRequest request);

    @Mapping(target = "departureAirport", source = "departureAirportCode")
    @Mapping(target = "arrivalAirport", source = "arrivalAirportCode")
    FlightResponse toResponse(Flight flight);

    default ZonedDateTime mapInstantToZonedDateTime(Instant instant) {
        return instant.atZone(ZoneId.systemDefault());
    }
}
