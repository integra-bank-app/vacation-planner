package integra.vacation_planner_backend.DTO;

import integra.vacation_planner_backend.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
// used by restaurant service to return restaurants data
public class RestaurantDTO {
    private final UUID id;
    private final String name;
    private final String address;
    private final LocalTime openingHour;
    private final LocalTime closingHour;
    private final Double rating = 5.0;

    public RestaurantDTO(Restaurant restaurant) {
        this.id = restaurant.getId();
        this.name = restaurant.getName();
        this.address = restaurant.getAddress();
        this.openingHour = restaurant.getOpeningHour();
        this.closingHour = restaurant.getClosingHour();
    }
}
