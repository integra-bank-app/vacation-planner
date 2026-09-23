package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.DTO.RestaurantDTO;
import integra.vacation_planner_backend.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/test")
    public String test() {
        return "test ca merge mappingul";
    }



}
