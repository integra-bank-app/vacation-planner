package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.DTO.RestaurantDTO;
import integra.vacation_planner_backend.entity.Restaurant;
import integra.vacation_planner_backend.repository.RestaurantRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RestaurantService {
    private RestaurantRepository restaurantRepository;

    public UUID createRestaurant(String name, String address, LocalTime openingHour, LocalTime closingHour) {
        // try to use a random ID 100 times
        for (int i = 1; i <= 100; i++) {
            UUID id = UUID.randomUUID();
            if (restaurantRepository.findById(id).isPresent()) {
                continue;
            }
            Restaurant restaurant = new Restaurant(id, name, address, openingHour, closingHour);
            restaurantRepository.save(restaurant);
            return id;
        }
        throw new RestaurantServiceException("could not find an id for the new restaurant");
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public List<RestaurantDTO> getRestaurantsByCity(String city) {
        // search for restaurants whose address contains the given city
        String query = "%" + city + "%";
        List<Restaurant> restaurants = restaurantRepository.getRestaurantsByAddressLike(query);

        List<RestaurantDTO> result = new ArrayList<RestaurantDTO>();
        for (Restaurant restaurant : restaurants) {
            result.add(new RestaurantDTO(restaurant));
        }
        return result;
    }

    public void updateRestaurant(UUID id, String address, LocalTime openingHour, LocalTime closingHour) {
        if (restaurantRepository.findById(id).isEmpty()) {
            throw new RestaurantServiceException("no restaurant with that id exists");
        }

        Restaurant restaurant = restaurantRepository.findById(id).get();
        if (address != null) {
            restaurant.setAddress(address);
        }
        if (openingHour != null) {
            restaurant.setOpeningHour(openingHour);
        }
        if (closingHour != null) {
            restaurant.setClosingHour(closingHour);
        }
        restaurantRepository.save(restaurant);
    }

    public void deleteRestaurant(UUID id) {
        if (restaurantRepository.findById(id).isEmpty()) {
            throw new RestaurantServiceException("could not find a restaurant with that id");
        }

        restaurantRepository.deleteById(id);
    }

}
