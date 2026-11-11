package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.RestaurantDTO;
import integra.vacation_planner_backend.exception.RestaurantServiceException;
import integra.vacation_planner_backend.service.RestaurantService;
import integra.vacation_planner_backend.dto.PageResponse;
import integra.vacation_planner_backend.exception.GlobalExceptionHandler;
import java.util.List;
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
import java.util.UUID;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
        mockMvc = MockMvcBuilders
            .standaloneSetup(new RestaurantController(restaurantServiceMock))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        restaurantDTO = new RestaurantDTO(
            UUID.randomUUID(),
            "Restaurant Central",
            "Str. Principala 1, Cluj",
            LocalTime.of(11, 11, 11),
            LocalTime.of(22, 22, 22),
            5.0
        );
    }

    @Test
    void getRestaurants() throws Exception {
        PageResponse<RestaurantDTO> response = new PageResponse<>(
            List.of(restaurantDTO),
            0,
            10,
            1L,
            1
        );

        when(restaurantServiceMock.getRestaurantsByCity("Cluj", 0, 10))
            .thenReturn(response);

        mockMvc.perform(get("/restaurants/Cluj"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id")
                .value(restaurantDTO.id().toString()))
            .andExpect(jsonPath("$.content[0].name")
                .value(restaurantDTO.name()))
            .andExpect(jsonPath("$.content[0].address")
                .value(restaurantDTO.address()))
            .andExpect(jsonPath("$.content[0].openingHour")
                .value(restaurantDTO.openingHour().toString()))
            .andExpect(jsonPath("$.content[0].closingHour")
                .value(restaurantDTO.closingHour().toString()))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));

        verify(restaurantServiceMock).getRestaurantsByCity("Cluj", 0, 10);
    }

    @Test
    void getRestaurants_shouldUseRequestedPageAndSize() throws Exception {
        PageResponse<RestaurantDTO> response = new PageResponse<>(
            List.of(restaurantDTO),
            1,
            5,
            6L,
            2
        );

        when(restaurantServiceMock.getRestaurantsByCity("Cluj", 1, 5))
            .thenReturn(response);

        mockMvc.perform(get("/restaurants/Cluj")
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(5))
            .andExpect(jsonPath("$.totalElements").value(6))
            .andExpect(jsonPath("$.totalPages").value(2));

        verify(restaurantServiceMock).getRestaurantsByCity("Cluj", 1, 5);
    }

    @Test
    void getRestaurants_shouldRejectInvalidSize() throws Exception {
        when(restaurantServiceMock.getRestaurantsByCity("Cluj", 0, 7))
            .thenThrow(new IllegalArgumentException(
                "size must be one of: 5, 10, 20, 50, 100"
            ));

        mockMvc.perform(get("/restaurants/Cluj")
                .param("size", "7"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "size must be one of: 5, 10, 20, 50, 100"
            ));
    }

    @Test
    void getRestaurants_shouldRejectNegativePage() throws Exception {
        when(restaurantServiceMock.getRestaurantsByCity("Cluj", -1, 10))
            .thenThrow(new IllegalArgumentException(
                "page must be greater than or equal to 0"
            ));

        mockMvc.perform(get("/restaurants/Cluj")
                .param("page", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "page must be greater than or equal to 0"
            ));
    }

    @Test
    void getRestaurants_shouldRejectNonNumericPage() throws Exception {
        mockMvc.perform(get("/restaurants/Cluj")
                .param("page", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "page must be a valid integer"
            ));

        verifyNoInteractions(restaurantServiceMock);
    }

    @Test
    void getRestaurants_shouldRejectNonNumericSize() throws Exception {
        mockMvc.perform(get("/restaurants/Cluj")
                .param("size", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "size must be a valid integer"
            ));

        verifyNoInteractions(restaurantServiceMock);
    }

    @Test
    void getRestaurants_shouldReturnEmptyContent_whenPageIsBeyondEnd()
        throws Exception {

        PageResponse<RestaurantDTO> response = new PageResponse<>(
            List.of(),
            3,
            10,
            25L,
            3
        );

        when(restaurantServiceMock.getRestaurantsByCity("Cluj", 3, 10))
            .thenReturn(response);

        mockMvc.perform(get("/restaurants/Cluj")
                .param("page", "3")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.page").value(3))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.totalPages").value(3));

        verify(restaurantServiceMock).getRestaurantsByCity("Cluj", 3, 10);
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
