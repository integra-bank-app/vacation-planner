package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.DTO.RestaurantDTO;
import integra.vacation_planner_backend.service.RestaurantService;
import integra.vacation_planner_backend.service.RestaurantServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    // get all restaurants from a city
    @GetMapping("/restaurants/{city}")
    public List<RestaurantDTO> getRestaurants(@PathVariable String city) {
        return restaurantService.getRestaurantsByCity(city);
    }

    // create a restauant
    @PostMapping("/restaurants")
    public UUID createRestaurant(@RequestParam String name, @RequestParam String address,
                                 @RequestParam LocalTime openingHour, @RequestParam LocalTime closingHour) {
        try {
            return restaurantService.createRestaurant(name, address, openingHour, closingHour);
        }
        catch (RestaurantServiceException e) {
            // TODO return error code
            return new UUID(0,0);
        }
    }

    // update a restaurnt
    // some fields may be missing, only updates existing fields
    @PutMapping("/restaurants/{id}")
    public String updateRestaurant(@PathVariable UUID id, @RequestParam Optional<String> address,
                                   @RequestParam Optional<LocalTime> openingHour, @RequestParam Optional<LocalTime> closingHour) {
        String add = null;
        LocalTime oh = null, ch = null;
        if (address.isPresent()) {
            add = address.get();
        }
        if (openingHour.isPresent()) {
            oh = openingHour.get();
        }
        if (closingHour.isPresent()) {
            ch = closingHour.get();
        }
        try {
            restaurantService.updateRestaurant(id, add, oh, ch);
            return "Restaurant updated successfully";
        }
        catch (RestaurantServiceException e) {
            return "Could not update restaurant: " + e.getMessage();
        }
    }

    // delete a restaurant
    @DeleteMapping("/restaurants/{id}")
    public String deleteRestaurant(@PathVariable UUID id) {
        try {
            restaurantService.deleteRestaurant(id);
            return "Restaurant deleted successfully";
        }
        catch (RestaurantServiceException e) {
            return "Could not delete restaurant: " + e.getMessage();
        }
    }
}
