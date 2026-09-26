package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.service.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TripController.class)
@WithMockUser
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TripService tripService;

    @Test
    void createTrip_shouldReturnTripId() throws Exception {
        UUID tripId = UUID.randomUUID();

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripService.createTrip(any(TripDTO.class))).thenReturn(tripId);

        mockMvc.perform(post("/trips")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().json("\"" + tripId + "\""));

        verify(tripService).createTrip(any(TripDTO.class));
    }

    @Test
    void createTrip_shouldReturnBadRequest_whenServiceRejectsRequest() throws Exception {
        TripDTO dto = new TripDTO();
        dto.setUsername("");
        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripService.createTrip(any(TripDTO.class)))
                .thenThrow(new IllegalArgumentException("username is required"));

        mockMvc.perform(post("/trips")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("username is required"));
    }

    @Test
    void getTrips_shouldReturnTrips() throws Exception {
        Trip trip = new Trip();
        trip.setId(UUID.randomUUID());
        trip.setUsername("bob");
        trip.setDestination("Paris");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripService.getTripsBetweenDates(
                eq("bob"),
                eq(LocalDate.of(2026, 6, 1)),
                eq(LocalDate.of(2026, 6, 30))
        )).thenReturn(List.of(trip));

        mockMvc.perform(get("/trips")
                        .param("username", "bob")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(trip.getId().toString()))
                .andExpect(jsonPath("$[0].username").value("bob"))
                .andExpect(jsonPath("$[0].destination").value("Paris"));
    }

    @Test
    void getTrips_shouldReturnEmptyList_whenNoTripsExist() throws Exception {
        when(tripService.getTripsBetweenDates(
                eq("bob"),
                eq(LocalDate.of(2026, 6, 1)),
                eq(LocalDate.of(2026, 6, 30))
        )).thenReturn(List.of());

        mockMvc.perform(get("/trips")
                        .param("username", "bob")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getTrips_shouldReturnBadRequest_whenUsernameMissing() throws Exception {
        mockMvc.perform(get("/trips")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrips_shouldReturnBadRequest_whenStartDateMissing() throws Exception {
        mockMvc.perform(get("/trips")
                        .param("username", "bob")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrips_shouldReturnBadRequest_whenEndDateMissing() throws Exception {
        mockMvc.perform(get("/trips")
                        .param("username", "bob")
                        .param("startDate", "2026-06-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrips_shouldReturnBadRequest_whenServiceRejectsRequest() throws Exception {
        when(tripService.getTripsBetweenDates(
                anyString(),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenThrow(new IllegalArgumentException("startDate must be before endDate"));

        mockMvc.perform(get("/trips")
                        .param("username", "bob")
                        .param("startDate", "2026-06-30")
                        .param("endDate", "2026-06-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("startDate must be before endDate"));
    }

    @Test
    void updateTrip_shouldReturnOk() throws Exception {
        UUID tripId = UUID.randomUUID();

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        mockMvc.perform(put("/trips/" + tripId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(tripService).updateTrip(eq(tripId), any(TripDTO.class));
    }

    @Test
    void updateTrip_shouldReturnNotFound_whenTripDoesNotExist() throws Exception {
        UUID tripId = UUID.randomUUID();

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        doThrow(new TripNotFoundException("Trip not found"))
                .when(tripService)
                .updateTrip(eq(tripId), any(TripDTO.class));

        mockMvc.perform(put("/trips/" + tripId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Trip not found"));
    }

    @Test
    void updateTrip_shouldReturnForbidden_whenUserIsNotOwner() throws Exception {
        UUID tripId = UUID.randomUUID();

        TripDTO dto = new TripDTO();
        dto.setUsername("alice");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        doThrow(new UnauthorizedTripAccessException("Not allowed to update this trip"))
                .when(tripService)
                .updateTrip(eq(tripId), any(TripDTO.class));

        mockMvc.perform(put("/trips/" + tripId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Not allowed to update this trip"));
    }

    @Test
    void updateTrip_shouldReturnBadRequest_whenServiceRejectsRequest() throws Exception {
        UUID tripId = UUID.randomUUID();

        TripDTO dto = new TripDTO();
        dto.setUsername("bob");

        doThrow(new IllegalArgumentException("At least one date must be provided"))
                .when(tripService)
                .updateTrip(eq(tripId), any(TripDTO.class));

        mockMvc.perform(put("/trips/" + tripId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("At least one date must be provided"));
    }

    @Test
    void updateTrip_shouldReturnBadRequest_whenUuidIsInvalid() throws Exception {
        TripDTO dto = new TripDTO();
        dto.setUsername("bob");
        dto.setStartDate(LocalDate.of(2026, 6, 12));

        mockMvc.perform(put("/trips/not-a-uuid")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTrip_shouldReturnOk() throws Exception {
        UUID tripId = UUID.randomUUID();

        mockMvc.perform(delete("/trips/" + tripId)
                        .with(csrf())
                        .param("username", "bob"))
                .andExpect(status().isOk());

        verify(tripService).deleteTrip(eq(tripId), eq("bob"));
    }

    @Test
    void deleteTrip_shouldReturnNotFound_whenTripDoesNotExist() throws Exception {
        UUID tripId = UUID.randomUUID();

        doThrow(new TripNotFoundException("Trip not found"))
                .when(tripService)
                .deleteTrip(eq(tripId), eq("bob"));

        mockMvc.perform(delete("/trips/" + tripId)
                        .with(csrf())
                        .param("username", "bob"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Trip not found"));
    }

    @Test
    void deleteTrip_shouldReturnForbidden_whenUserIsNotOwner() throws Exception {
        UUID tripId = UUID.randomUUID();

        doThrow(new UnauthorizedTripAccessException("Not allowed to delete this trip"))
                .when(tripService)
                .deleteTrip(eq(tripId), eq("alice"));

        mockMvc.perform(delete("/trips/" + tripId)
                        .with(csrf())
                        .param("username", "alice"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Not allowed to delete this trip"));
    }

    @Test
    void deleteTrip_shouldReturnBadRequest_whenUsernameMissing() throws Exception {
        UUID tripId = UUID.randomUUID();

        mockMvc.perform(delete("/trips/" + tripId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTrip_shouldReturnBadRequest_whenUuidIsInvalid() throws Exception {
        mockMvc.perform(delete("/trips/not-a-uuid")
                        .with(csrf())
                        .param("username", "bob"))
                .andExpect(status().isBadRequest());
    }
}