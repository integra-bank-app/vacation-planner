package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.repository.RestaurantRepository;
import integra.vacation_planner_backend.validator.RestaurantValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    @Mock
    RestaurantRepository restaurantRepositoryMock;
    @InjectMocks
    RestaurantService restaurantService;

    @Test
    void createRestaurant() {
        String name = "name";
        String address = "address";
        LocalTime openingHour = LocalTime.of(8, 0);
        LocalTime closingHour = LocalTime.of(22, 0);

        restaurantService.createRestaurant(name, address, openingHour, closingHour);
        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepositoryMock).save(captor.capture());
        Restaurant restaurant = captor.getValue();
        assertEquals(name, restaurant.getName());



        // TODO vezi cum pot verifica ca se apeleaza o metoda statica

    }

    @Test
    void getAllRestaurants() {
    }

    @Test
    void getRestaurantsByCity() {
    }

    @Test
    void updateRestaurant() {
    }

    @Test
    void deleteRestaurant() {
    }
}