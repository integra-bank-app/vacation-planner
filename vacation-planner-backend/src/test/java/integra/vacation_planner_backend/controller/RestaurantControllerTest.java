package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.RestaurantDTO;
import integra.vacation_planner_backend.exception.RestaurantServiceException;
import integra.vacation_planner_backend.service.RestaurantService;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class RestaurantControllerTest {
    @Mock
    private RestaurantService restaurantServiceMock;

    private RestaurantDTO restaurantDTO;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RestaurantController(restaurantServiceMock)).build();
        restaurantDTO = new RestaurantDTO(UUID.randomUUID(), RandomString.make(), RandomString.make(),
                LocalTime.of(11, 11, 11), LocalTime.of(22, 22, 22), 5.0);
    }

    @Test
    void getRestaurants() throws Exception {
        ArrayList<RestaurantDTO> restaurantDTOList = new ArrayList<>();
        restaurantDTOList.add(restaurantDTO);
        when(restaurantServiceMock.getRestaurantsByCity(any())).thenReturn(restaurantDTOList);

        mockMvc.perform(get("/restaurants/Cluj"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(restaurantDTO.id().toString()))
                .andExpect(jsonPath("$[0].name").value(restaurantDTO.name()))
                .andExpect(jsonPath("$[0].address").value(restaurantDTO.address()))
                .andExpect(jsonPath("$[0].openingHour").value(restaurantDTO.openingHour().toString()))
                .andExpect(jsonPath("$[0].closingHour").value(restaurantDTO.closingHour().toString()));
    }

    @Test
    void createRestaurant() throws Exception {
        when(restaurantServiceMock.createRestaurant(eq(restaurantDTO.name()), eq(restaurantDTO.address()), any(), any()))
                .thenReturn(restaurantDTO.id());

        mockMvc.perform(post("/restaurants").param("name", restaurantDTO.name())
                        .param("address", restaurantDTO.address())
                        .param("openingHour", restaurantDTO.openingHour().toString())
                        .param("closingHour", restaurantDTO.closingHour().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(restaurantDTO.id().toString()));

        when(restaurantServiceMock.createRestaurant(eq(restaurantDTO.name()), eq(restaurantDTO.address()), any(), any()))
                .thenThrow(RestaurantServiceException.class);

        mockMvc.perform(post("/restaurants").param("name", restaurantDTO.name())
                        .param("address", restaurantDTO.address())
                        .param("openingHour", restaurantDTO.openingHour().toString())
                        .param("closingHour", restaurantDTO.closingHour().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRestaurant() throws Exception {
        String newName = RandomString.make(20);
        mockMvc.perform(put("/restaurants/" + restaurantDTO.id())
                        .param("name", newName))
                .andExpect(status().isOk());

        UUID id_for_error = UUID.randomUUID();
        doThrow(new RestaurantServiceException()).when(restaurantServiceMock).updateRestaurant(eq(id_for_error), any(), any(), any());

        mockMvc.perform(put("/restaurants/" + id_for_error)
                        .param("name", newName))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRestaurant() throws Exception {
        mockMvc.perform(delete("/restaurants/" + UUID.randomUUID()))
                .andExpect(status().isOk());

        UUID id_for_error = UUID.randomUUID();
        doThrow(new RestaurantServiceException()).when(restaurantServiceMock).deleteRestaurant(id_for_error);

        mockMvc.perform(delete("/restaurants/" + id_for_error))
                .andExpect(status().isNotFound());
    }
}
