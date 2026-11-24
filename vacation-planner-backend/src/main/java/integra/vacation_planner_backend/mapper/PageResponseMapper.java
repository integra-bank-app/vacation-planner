package integra.vacation_planner_backend.mapper;

import integra.vacation_planner_backend.dto.PageResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.ArrayList;

public interface PageResponseMapper<T> {
    @Mapping(target = "page", source = "number")
    PageResponse<T> toPageResponse(Page<T> page);

    @AfterMapping
    default PageResponse<T> postProcessing(@MappingTarget PageResponse<T> response) {
        if (response.content() == null) {
            return new PageResponse<T>(new ArrayList<T>(), response.page(), response.size(),
                response.totalElements(), response.totalPages());
        }
        return response;
    }
}
