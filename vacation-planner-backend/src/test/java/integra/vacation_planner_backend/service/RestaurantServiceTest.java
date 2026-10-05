package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.exception.RestaurantServiceException;
import integra.vacation_planner_backend.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    @Mock
    private RestaurantRepository restaurantRepositoryMock;
    @InjectMocks
    private RestaurantService restaurantService;

    @Test
    void createRestaurant() {
        String name = "name";
        String address = "address";
        LocalTime openingHour = LocalTime.of(8, 0);
        LocalTime closingHour = LocalTime.of(22, 0);

        UUID id = restaurantService.createRestaurant(name, address, openingHour, closingHour);
        ArgumentCaptor<Restaurant> restaurantArgumentCaptor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepositoryMock).save(restaurantArgumentCaptor.capture());

        Restaurant restaurant = restaurantArgumentCaptor.getValue();
        assertEquals(id, restaurant.getId());
        assertEquals(name, restaurant.getName());
        assertEquals(address, restaurant.getAddress());
        assertEquals(openingHour, restaurant.getOpeningHour());
        assertEquals(closingHour, restaurant.getClosingHour());
    }

    @Test
    void getAllRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        Restaurant restaurant1 = new Restaurant(UUID.randomUUID(), "name1111", "address1111",
                LocalTime.of(11, 11), LocalTime.of(21, 11));
        Restaurant restaurant2 = new Restaurant(UUID.randomUUID(), "name2222", "address2222",
                LocalTime.of(12, 22), LocalTime.of(22, 22));
        restaurants.add(restaurant1);
        restaurants.add(restaurant2);
        when(restaurantRepositoryMock.findAll()).thenReturn(restaurants);
        assertEquals(restaurants, restaurantService.getAllRestaurants());
    }

    @Test
    void getRestaurantsByCity() {
        List<Restaurant> restaurants = new ArrayList<>();
        Restaurant restaurant = new Restaurant(UUID.randomUUID(), "name", "Cluj", LocalTime.NOON, LocalTime.MIDNIGHT);
        restaurants.add(restaurant);

        when(restaurantRepositoryMock.getRestaurantsByAddressLike("Cluj")).thenReturn(restaurants);
        assertEquals(restaurants, restaurantRepositoryMock.getRestaurantsByAddressLike("Cluj"));
    }

    @Test
    void updateRestaurant() {
        UUID id1 = UUID.randomUUID();
        when(restaurantRepositoryMock.findById(id1)).thenReturn(Optional.empty());
        assertThrows(RestaurantServiceException.class, () -> restaurantService.updateRestaurant
                (id1, "address", LocalTime.of(12, 0), LocalTime.of(22, 0)));

        UUID id2 = UUID.randomUUID();
        String name = "name";
        String oldAddress = "old address";
        LocalTime oldOpeningHour = LocalTime.of(8, 0);
        LocalTime oldClosingHour = LocalTime.of(20, 0);

        Restaurant oldRestaurant = new Restaurant(id2, name, oldAddress, oldOpeningHour, oldClosingHour);
        when(restaurantRepositoryMock.findById(id2)).thenReturn(Optional.of(oldRestaurant));

        String newAddress = "new address";
        LocalTime newOpeningHour = LocalTime.of(10, 0);
        LocalTime newClosingHour = LocalTime.of(22, 0);

        restaurantService.updateRestaurant(id2, newAddress, newOpeningHour, newClosingHour);
        Restaurant newRestaurant = new Restaurant(id2, name, newAddress, newOpeningHour, newClosingHour);
        ArgumentCaptor<Restaurant> restaurantArgumentCaptor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepositoryMock).save(restaurantArgumentCaptor.capture());
        assertEquals(newRestaurant, restaurantArgumentCaptor.getValue());
    }

    @Test
    void deleteRestaurant() {
        UUID id = UUID.randomUUID();
        when(restaurantRepositoryMock.findById(id)).thenReturn(Optional.empty());
        assertThrows(RestaurantServiceException.class, () -> restaurantService.deleteRestaurant(id));

        when(restaurantRepositoryMock.findById(id)).thenReturn(Optional.of(new Restaurant(id, "", "", LocalTime.NOON, LocalTime.NOON)));
        restaurantService.deleteRestaurant(id);
        ArgumentCaptor<UUID> UUIDArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(restaurantRepositoryMock).deleteById(UUIDArgumentCaptor.capture());
        assertEquals(id, UUIDArgumentCaptor.getValue());
    }
}