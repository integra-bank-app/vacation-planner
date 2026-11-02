
package integra.vacation_planner_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import integra.vacation_planner_backend.dto.TripRestaurantRequestDTO;
import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.service.TripRestaurantService;
import integra.vacation_planner_backend.service.TripService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TripController.class)
@AutoConfigureMockMvc(addFilters = false)
class TripRestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules();

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private TripRestaurantService tripRestaurantService;

    @Test
    void addRestaurantToTrip_shouldReturnOk_whenRequestIsValid()
        throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();

        TripRestaurantRequestDTO request = new TripRestaurantRequestDTO(
            "alice",
            restaurantId,
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        when(tripRestaurantService.addRestaurantToTrip(
            eq(tripId),
            eq("alice"),
            eq(restaurantId),
            eq(LocalDate.of(2026, 6, 12)),
            eq(LocalTime.of(19, 0))
        )).thenReturn(visitId);

        mockMvc.perform(post("/trips/{tripId}/restaurants", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(tripRestaurantService).addRestaurantToTrip(
            tripId,
            "alice",
            restaurantId,
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );
    }

    @Test
    void addRestaurantToTrip_shouldReturnNotFound_whenTripDoesNotExist()
        throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        TripRestaurantRequestDTO request = new TripRestaurantRequestDTO(
            "alice",
            restaurantId,
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        when(tripRestaurantService.addRestaurantToTrip(
            eq(tripId),
            eq("alice"),
            eq(restaurantId),
            any(LocalDate.class),
            any(LocalTime.class)
        )).thenThrow(new TripNotFoundException("Trip not found"));

        mockMvc.perform(post("/trips/{tripId}/restaurants", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void addRestaurantToTrip_shouldReturnForbidden_whenUserIsNotOwner()
        throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        TripRestaurantRequestDTO request = new TripRestaurantRequestDTO(
            "alice",
            restaurantId,
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        when(tripRestaurantService.addRestaurantToTrip(
            eq(tripId),
            eq("alice"),
            eq(restaurantId),
            any(LocalDate.class),
            any(LocalTime.class)
        )).thenThrow(
            new UnauthorizedTripAccessException("Not your trip")
        );

        mockMvc.perform(post("/trips/{tripId}/restaurants", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void addRestaurantToTrip_shouldReturnConflict_whenVisitAlreadyExists()
        throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        TripRestaurantRequestDTO request = new TripRestaurantRequestDTO(
            "alice",
            restaurantId,
            LocalDate.of(2026, 6, 12),
            LocalTime.of(19, 0)
        );

        when(tripRestaurantService.addRestaurantToTrip(
            eq(tripId),
            eq("alice"),
            eq(restaurantId),
            any(LocalDate.class),
            any(LocalTime.class)
        )).thenThrow(
            new ConflictException("Visit already exists")
        );

        mockMvc.perform(post("/trips/{tripId}/restaurants", tripId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void getRestaurantsForTrip_shouldReturnVisits_whenUserIsOwner()
        throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();

        TripRestaurantResponseDTO response =
            new TripRestaurantResponseDTO(
                visitId,
                restaurantId,
                "Test Restaurant",
                "Main Street",
                LocalDate.of(2026, 6, 12),
                LocalTime.of(19, 0)
            );

        when(tripRestaurantService.getRestaurantsForTrip(tripId, "alice"))
            .thenReturn(List.of(response));

        mockMvc.perform(get("/trips/{tripId}/restaurants", tripId)
                .param("username", "alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(visitId.toString()))
            .andExpect(jsonPath("$[0].restaurantId")
                .value(restaurantId.toString()))
            .andExpect(jsonPath("$[0].restaurantName")
                .value("Test Restaurant"))
            .andExpect(jsonPath("$[0].visitDate").value("2026-06-12"))
            .andExpect(jsonPath("$[0].visitTime").value("19:00:00"));
    }

    @Test
    void getRestaurantsForTrip_shouldReturnForbidden_whenUserIsNotOwner()
        throws Exception {
        UUID tripId = UUID.randomUUID();

        when(tripRestaurantService.getRestaurantsForTrip(tripId, "alice"))
            .thenThrow(
                new UnauthorizedTripAccessException("Not your trip")
            );

        mockMvc.perform(get("/trips/{tripId}/restaurants", tripId)
                .param("username", "alice"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getRestaurantsForTrip_shouldReturnNotFound_whenTripDoesNotExist()
        throws Exception {
        UUID tripId = UUID.randomUUID();

        when(tripRestaurantService.getRestaurantsForTrip(tripId, "alice"))
            .thenThrow(new TripNotFoundException("Trip not found"));

        mockMvc.perform(get("/trips/{tripId}/restaurants", tripId)
                .param("username", "alice"))
            .andExpect(status().isNotFound());
    }

    @Test
    void removeRestaurantFromTrip_shouldReturnOk_whenVisitExists()
        throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();

        doNothing().when(tripRestaurantService)
            .removeRestaurantFromTrip(tripId, visitId, "alice");

        mockMvc.perform(delete(
                "/trips/{tripId}/restaurants/{visitId}",
                tripId,
                visitId
            ).param("username", "alice"))
            .andExpect(status().isOk());

        verify(tripRestaurantService).removeRestaurantFromTrip(
            tripId, visitId, "alice"
        );
    }

    @Test
    void removeRestaurantFromTrip_shouldReturnNotFound_whenVisitDoesNotExist()
        throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();

        doThrow(new NotFoundException("Visit not found"))
            .when(tripRestaurantService)
            .removeRestaurantFromTrip(tripId, visitId, "alice");

        mockMvc.perform(delete(
                "/trips/{tripId}/restaurants/{visitId}",
                tripId,
                visitId
            ).param("username", "alice"))
            .andExpect(status().isNotFound());
    }

    @Test
    void removeRestaurantFromTrip_shouldReturnForbidden_whenUserIsNotOwner()
        throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID visitId = UUID.randomUUID();

        doThrow(new UnauthorizedTripAccessException("Not your trip"))
            .when(tripRestaurantService)
            .removeRestaurantFromTrip(tripId, visitId, "alice");

        mockMvc.perform(delete(
                "/trips/{tripId}/restaurants/{visitId}",
                tripId,
                visitId
            ).param("username", "alice"))
            .andExpect(status().isForbidden());
    }
}
