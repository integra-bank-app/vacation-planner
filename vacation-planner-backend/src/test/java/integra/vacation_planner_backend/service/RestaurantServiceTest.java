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
import integra.vacation_planner_backend.dto.PageResponse;
import integra.vacation_planner_backend.dto.RestaurantDTO;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

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
        Restaurant restaurant = new Restaurant(
            UUID.randomUUID(),
            "Restaurant Central",
            "Str. Principala 1, Cluj",
            LocalTime.NOON,
            LocalTime.of(22, 0)
        );

        PageRequest pageable = PageRequest.of(
            1,
            10,
            Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
            )
        );

        when(restaurantRepositoryMock.getRestaurantsByAddressLike(
            "%Cluj%",
            pageable
        )).thenReturn(
            new PageImpl<>(List.of(restaurant), pageable, 25)
        );

        PageResponse<RestaurantDTO> result =
            restaurantService.getRestaurantsByCity("Cluj", 1, 10);

        assertEquals(
            List.of(new RestaurantDTO(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getOpeningHour(),
                restaurant.getClosingHour(),
                5.0
            )),
            result.content()
        );
        assertEquals(1, result.page());
        assertEquals(10, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());

        verify(restaurantRepositoryMock).getRestaurantsByAddressLike(
            "%Cluj%",
            pageable
        );
    }

    @Test
    void getRestaurantsByCity_pageBeyondEnd_returnsEmptyContentAndKeepsTotals() {
        PageRequest pageable = PageRequest.of(
            3,
            10,
            Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.asc("id")
            )
        );

        when(restaurantRepositoryMock.getRestaurantsByAddressLike(
            "%Cluj%",
            pageable
        )).thenReturn(
            new PageImpl<Restaurant>(List.of(), pageable, 25)
        );

        PageResponse<RestaurantDTO> result =
            restaurantService.getRestaurantsByCity("Cluj", 3, 10);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.page());
        assertEquals(10, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());

        verify(restaurantRepositoryMock).getRestaurantsByAddressLike(
            "%Cluj%",
            pageable
        );
    }

    @Test
    void getRestaurantsByCity_negativePage_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> restaurantService.getRestaurantsByCity("Cluj", -1, 10)
        );

        assertEquals(
            "page must be greater than or equal to 0",
            exception.getMessage()
        );

        verifyNoInteractions(restaurantRepositoryMock);
    }

    @Test
    void getRestaurantsByCity_invalidSizes_throwIllegalArgumentException() {
        for (int size : List.of(-1, 0, 1, 7, 15, 101)) {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> restaurantService.getRestaurantsByCity("Cluj", 0, size)
            );

            assertEquals(
                "size must be one of: 5, 10, 20, 50, 100",
                exception.getMessage()
            );
        }

        verifyNoInteractions(restaurantRepositoryMock);
    }

    @Test
    void getRestaurantsByCity_allowedSizes_arePassedToRepository() {
        for (int size : List.of(5, 10, 20, 50, 100)) {
            PageRequest pageable = PageRequest.of(
                0,
                size,
                Sort.by(
                    Sort.Order.asc("name"),
                    Sort.Order.asc("id")
                )
            );

            when(restaurantRepositoryMock.getRestaurantsByAddressLike(
                "%Cluj%",
                pageable
            )).thenReturn(
                new PageImpl<Restaurant>(List.of(), pageable, 0)
            );

            PageResponse<RestaurantDTO> result =
                restaurantService.getRestaurantsByCity("Cluj", 0, size);

            assertTrue(result.content().isEmpty());
            assertEquals(0, result.page());
            assertEquals(size, result.size());
            assertEquals(0L, result.totalElements());
            assertEquals(0, result.totalPages());

            verify(restaurantRepositoryMock).getRestaurantsByAddressLike(
                "%Cluj%",
                pageable
            );
        }
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
