package integra.vacation_planner_backend.validator;

import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.exception.ValidatorException;

public class RestaurantValidator {
    public static void validate (Restaurant restaurant) {
        if (restaurant.getName().length() > 50) throw new ValidatorException("Name must be at most 50 characters");
        if (restaurant.getAddress().length() > 100) throw new ValidatorException("Address must be at most 100 characters");
        if (restaurant.getName().isEmpty()) throw new ValidatorException("Restaurant must have a name");
        if (restaurant.getAddress().isEmpty()) throw new ValidatorException("Restaurant must have an address");
    }
}
