package integra.vacation_planner_backend.validator;

import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.exception.ValidatorException;

public class RestaurantValidator {
    public static void validate (Restaurant restaurant) {
        if (restaurant.getName() == null || restaurant.getName().isEmpty()) {
            throw new ValidatorException("The restaurant must have a name");
        }
        if (restaurant.getName().length() > 50) {
            throw new ValidatorException("Then ame must be at most 50 characters");
        }

        if (restaurant.getAddress() == null || restaurant.getAddress().isEmpty()) {
            throw new ValidatorException("The restaurant must have an address");
        }
        if (restaurant.getAddress().length() > 100) {
            throw new ValidatorException("The address must be at most 100 characters");
        }

        if (restaurant.getOpeningHour() == null) {
            throw new ValidatorException("The restaurant must have an opening hour");
        }
        if (restaurant.getClosingHour() == null) {
            throw new ValidatorException("The restaurant must have a closing hour");
        }
    }
}
