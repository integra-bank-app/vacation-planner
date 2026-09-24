package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.RestaurantDTO;
import integra.vacation_planner_backend.service.RestaurantService;
import integra.vacation_planner_backend.exception.RestaurantServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @Autowired
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/restaurants/{city}")
    public List<RestaurantDTO> getRestaurants(@PathVariable String city) {
        return restaurantService.getRestaurantsByCity(city);
    }

    @PostMapping("/restaurants")
    public UUID createRestaurant(@RequestParam String name, @RequestParam String address,
                                 @RequestParam LocalTime openingHour, @RequestParam LocalTime closingHour) {
        try {
            return restaurantService.createRestaurant(name, address, openingHour, closingHour);
        }
        catch (RestaurantServiceException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/restaurants/{id}")
    public ResponseEntity<String> updateRestaurant(@PathVariable UUID id, @RequestParam Optional<String> address,
                                   @RequestParam Optional<LocalTime> openingHour, @RequestParam Optional<LocalTime> closingHour) {
        try {
            restaurantService.updateRestaurant(id, address.orElse(null), openingHour.orElse(null),
                    closingHour.orElse(null));
            return ResponseEntity.ok("Restaurant updated successfully");
        }
        catch (RestaurantServiceException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable UUID id) {
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.ok("Restaurant deleted successfully");
        }
        catch (RestaurantServiceException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
