package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.RestaurantDTO;
import integra.vacation_planner_backend.service.RestaurantService;
import integra.vacation_planner_backend.exception.RestaurantServiceException;
import integra.vacation_planner_backend.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @GetMapping("/{city}")
    @Operation(
        summary = "Search restaurants by city",
        description = "Results are sorted by name ascending, then id ascending."
    )
    public PageResponse<RestaurantDTO> getRestaurants(
        @PathVariable String city,
        @Parameter(
            description = "Page number, starting at 0",
            schema = @Schema(
                type = "integer",
                minimum = "0",
                defaultValue = "0"
            )
        )
        @RequestParam(defaultValue = "0") int page,
        @Parameter(
            description = "Number of items per page",
            schema = @Schema(
                type = "integer",
                defaultValue = "10",
                allowableValues = {"5", "10", "20", "50", "100"}
            )
        )
        @RequestParam(defaultValue = "10") int size
    ) {
        return restaurantService.getRestaurantsByCity(city, page, size);
    }

    @PostMapping()
    public UUID createRestaurant(@RequestParam String name, @RequestParam String address,
                                 @RequestParam LocalTime openingHour, @RequestParam LocalTime closingHour) {
        try {
            return restaurantService.createRestaurant(name, address, openingHour, closingHour);
        }
        catch (RestaurantServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public void updateRestaurant(@PathVariable UUID id, @RequestParam Optional<String> address,
                                   @RequestParam Optional<LocalTime> openingHour, @RequestParam Optional<LocalTime> closingHour) {
        try {
            restaurantService.updateRestaurant(id, address.orElse(null), openingHour.orElse(null),
                    closingHour.orElse(null));
        }
        catch (RestaurantServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteRestaurant(@PathVariable UUID id) {
        try {
            restaurantService.deleteRestaurant(id);
        }
        catch (RestaurantServiceException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
