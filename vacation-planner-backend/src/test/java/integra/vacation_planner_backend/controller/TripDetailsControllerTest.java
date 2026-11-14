package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.dto.TimelineDayResponse;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
import integra.vacation_planner_backend.dto.TripDetailsResponse;
import integra.vacation_planner_backend.dto.TripOwnerResponse;
import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.service.TripRestaurantService;
import integra.vacation_planner_backend.service.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TripController.class)
@WithMockUser
class TripDetailsControllerTest {

    private final UUID tripId = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private TripRestaurantService tripRestaurantService;

    @Test
    void getTripDetails_returnsTheWholeTrip() throws Exception {
        LocalDate day = LocalDate.of(2026, 7, 1);
        FlightResponse flight = new FlightResponse(
            "RO101", "CLJ", "CDG",
            ZonedDateTime.of(2026, 7, 1, 10, 0, 0, 0, ZoneId.of("UTC")),
            ZonedDateTime.of(2026, 7, 1, 12, 0, 0, 0, ZoneId.of("UTC")),
            new BigDecimal("120.00")
        );
        TripRestaurantResponseDTO visit = new TripRestaurantResponseDTO(
            UUID.randomUUID(), UUID.randomUUID(), "Roma Trattoria", "Via Roma 10", day, LocalTime.of(19, 0)
        );
        TripDetailsResponse response = new TripDetailsResponse(
            tripId, "Paris", day, LocalDate.of(2026, 7, 2),
            new TripOwnerResponse("ana.pop", "Ana", "Pop"),
            List.of(new TripAccommodationResponse(
                UUID.randomUUID(), "Paris Stay", "Rue de Paris 22", "Paris", new BigDecimal("150.00")
            )),
            List.of(flight),
            List.of(visit),
            List.of(
                new TimelineDayResponse(day, List.of(flight), List.of(visit)),
                new TimelineDayResponse(LocalDate.of(2026, 7, 2), List.of(), List.of())
            )
        );
        when(tripService.getTripDetails(tripId, "ana.pop")).thenReturn(response);

        mockMvc.perform(get("/trips/" + tripId).param("username", "ana.pop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(tripId.toString()))
            .andExpect(jsonPath("$.destination").value("Paris"))
            .andExpect(jsonPath("$.startDate").value("2026-07-01"))
            .andExpect(jsonPath("$.endDate").value("2026-07-02"))
            .andExpect(jsonPath("$.owner.username").value("ana.pop"))
            .andExpect(jsonPath("$.owner.firstName").value("Ana"))
            .andExpect(jsonPath("$.owner.lastName").value("Pop"))
            .andExpect(jsonPath("$.accommodations[0].name").value("Paris Stay"))
            .andExpect(jsonPath("$.accommodations[0].pricePerNight").value(150.00))
            .andExpect(jsonPath("$.flights[0].flightNumber").value("RO101"))
            .andExpect(jsonPath("$.flights[0].price").value(120.00))
            .andExpect(jsonPath("$.restaurants[0].restaurantName").value("Roma Trattoria"))
            .andExpect(jsonPath("$.timeline.length()").value(2))
            .andExpect(jsonPath("$.timeline[0].date").value("2026-07-01"))
            .andExpect(jsonPath("$.timeline[0].flights[0].flightNumber").value("RO101"))
            .andExpect(jsonPath("$.timeline[0].restaurants[0].restaurantName").value("Roma Trattoria"))
            .andExpect(jsonPath("$.timeline[1].flights").isEmpty());

        verify(tripService).getTripDetails(tripId, "ana.pop");
    }

    @Test
    void getTripDetails_returnsEmptyArraysForATripWithNothingLinked() throws Exception {
        LocalDate day = LocalDate.of(2026, 7, 1);
        TripDetailsResponse response = new TripDetailsResponse(
            tripId, "Paris", day, day,
            new TripOwnerResponse("ana.pop", "Ana", "Pop"),
            List.of(), List.of(), List.of(),
            List.of(new TimelineDayResponse(day, List.of(), List.of()))
        );
        when(tripService.getTripDetails(tripId, "ana.pop")).thenReturn(response);

        mockMvc.perform(get("/trips/" + tripId).param("username", "ana.pop"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accommodations").isArray())
            .andExpect(jsonPath("$.accommodations").isEmpty())
            .andExpect(jsonPath("$.flights").isArray())
            .andExpect(jsonPath("$.flights").isEmpty())
            .andExpect(jsonPath("$.restaurants").isArray())
            .andExpect(jsonPath("$.restaurants").isEmpty())
            .andExpect(jsonPath("$.timeline.length()").value(1));
    }

    @Test
    void getTripDetails_returns404ForAnUnknownTrip() throws Exception {
        when(tripService.getTripDetails(tripId, "ana.pop"))
            .thenThrow(new TripNotFoundException("Trip not found"));

        mockMvc.perform(get("/trips/" + tripId).param("username", "ana.pop"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Trip not found"));
    }

    @Test
    void getTripDetails_returns403WhenTheUserIsNotTheOwner() throws Exception {
        when(tripService.getTripDetails(tripId, "mallory"))
            .thenThrow(new UnauthorizedTripAccessException("User is not authorized to view this trip"));

        mockMvc.perform(get("/trips/" + tripId).param("username", "mallory"))
            .andExpect(status().isForbidden())
            .andExpect(content().string("User is not authorized to view this trip"));
    }

    @Test
    void getTripDetails_returns400WithoutAUsername() throws Exception {
        mockMvc.perform(get("/trips/" + tripId))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getTripDetails_returns400ForAnInvalidTripId() throws Exception {
        mockMvc.perform(get("/trips/not-a-uuid").param("username", "ana.pop"))
            .andExpect(status().isBadRequest());
    }
}
