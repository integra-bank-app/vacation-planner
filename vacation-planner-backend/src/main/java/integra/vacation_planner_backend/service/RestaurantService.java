package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.dto.RestaurantDTO;
import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.exception.RestaurantServiceException;
import integra.vacation_planner_backend.repository.RestaurantRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RestaurantService {
    private RestaurantRepository restaurantRepository;

    public UUID createRestaurant(String name, String address, LocalTime openingHour, LocalTime closingHour) {
        UUID id = UUID.randomUUID();
        // TODO validate data and throw exception
        Restaurant restaurant = new Restaurant(id, name, address, openingHour, closingHour);
        restaurantRepository.save(restaurant);
        return id;
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    // fixme implement a better way to check the address
    public List<RestaurantDTO> getRestaurantsByCity(String city) {
        // search for restaurants whose address contains the given city
        String query = "%" + city + "%";
        return restaurantRepository.getRestaurantsByAddressLike(query).stream().map(it -> new RestaurantDTO
                (it.getId(), it.getName(), it.getAddress(), it.getOpeningHour(), it.getClosingHour(), 5.0)).toList();
    }

    public void updateRestaurant(UUID id, String address, LocalTime openingHour, LocalTime closingHour) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(
                () -> new RestaurantServiceException("no restaurant with that id found"));

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
