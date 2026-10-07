package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.RestaurantDTO;
import integra.vacation_planner_backend.service.RestaurantService;
import integra.vacation_planner_backend.exception.RestaurantServiceException;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @GetMapping("/{city}")
    public List<RestaurantDTO> getRestaurants(@PathVariable String city) {
        return restaurantService.getRestaurantsByCity(city);
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
