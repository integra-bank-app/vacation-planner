package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.dto.RestaurantDTO;
import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.exception.RestaurantServiceException;
import integra.vacation_planner_backend.repository.RestaurantRepository;
import integra.vacation_planner_backend.validator.RestaurantValidator;
import integra.vacation_planner_backend.dto.PageResponse;
import integra.vacation_planner_backend.pagination.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        Restaurant restaurant = new Restaurant(id, name, address, openingHour, closingHour);
        RestaurantValidator.validate(restaurant);
        restaurantRepository.save(restaurant);
        return id;
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public PageResponse<RestaurantDTO> getRestaurantsByCity(
        String city,
        int page,
        int size
    ) {
        PageRequest pageable = Pagination.create(
            page,
            size,
            Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
            )
        );

        // search for restaurants whose address contains the given city
        String query = "%" + city + "%";

        Page<RestaurantDTO> restaurants = restaurantRepository
            .getRestaurantsByAddressLike(query, pageable)
            .map(it -> new RestaurantDTO(
                it.getId(),
                it.getName(),
                it.getAddress(),
                it.getOpeningHour(),
                it.getClosingHour(),
                5.0
            ));

        return new PageResponse<>(
            restaurants.getContent(),
            restaurants.getNumber(),
            restaurants.getSize(),
            restaurants.getTotalElements(),
            restaurants.getTotalPages()
        );
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
        RestaurantValidator.validate(restaurant);
        restaurantRepository.save(restaurant);
    }

    public void deleteRestaurant(UUID id) {
        if (restaurantRepository.findById(id).isEmpty()) {
            throw new RestaurantServiceException("could not find a restaurant with that id");
        }

        restaurantRepository.deleteById(id);
    }

}
