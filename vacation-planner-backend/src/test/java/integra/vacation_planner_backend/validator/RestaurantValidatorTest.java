package integra.vacation_planner_backend.validator;

import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.exception.ValidatorException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestaurantValidatorTest {
    @Test
    void validate() {
        String okName = "a".repeat(50);
        String longName = "a".repeat(51);
        String okAddress = "a".repeat(100);
        String longAddress = "a".repeat(101);
        LocalTime openingHour = LocalTime.of(12, 0);
        LocalTime closingHour = LocalTime.of(22, 0);

        Restaurant okRestaurant = new Restaurant(UUID.randomUUID(), okName, okAddress, openingHour, closingHour);
        Restaurant longNameRestaurant = new Restaurant(UUID.randomUUID(), longName, okAddress, openingHour, closingHour);
        Restaurant longAddressRestaurant = new Restaurant(UUID.randomUUID(), okName, longAddress, openingHour, closingHour);
        Restaurant nullNameRestaurant = new Restaurant(UUID.randomUUID(), null, okAddress, openingHour, closingHour);
        Restaurant nullAddressRestaurant = new Restaurant(UUID.randomUUID(), okName, null, openingHour, closingHour);
        Restaurant nullOpeningHourRestaurant = new Restaurant(UUID.randomUUID(), okName, okAddress, null, closingHour);
        Restaurant nullClosingHourRestaurant = new Restaurant(UUID.randomUUID(), okName, okAddress, openingHour, null);

        assertDoesNotThrow(() -> RestaurantValidator.validate(okRestaurant));
        assertThrows(ValidatorException.class, () -> RestaurantValidator.validate(longNameRestaurant));
        assertThrows(ValidatorException.class, () -> RestaurantValidator.validate(longAddressRestaurant));
        assertThrows(ValidatorException.class, () -> RestaurantValidator.validate(nullNameRestaurant));
        assertThrows(ValidatorException.class, () -> RestaurantValidator.validate(nullAddressRestaurant));
        assertThrows(ValidatorException.class, () -> RestaurantValidator.validate(nullOpeningHourRestaurant));
        assertThrows(ValidatorException.class, () -> RestaurantValidator.validate(nullClosingHourRestaurant));
    }
}
