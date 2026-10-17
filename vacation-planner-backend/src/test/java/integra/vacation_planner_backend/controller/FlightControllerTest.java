package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.dto.PageResponse;
import integra.vacation_planner_backend.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightService flightService;

    @Test
    void createFlight_returnsCreatedFlight() throws Exception {
        when(flightService.createFlight(any(FlightRequest.class)))
            .thenReturn(flight(180));

        mockMvc.perform(post("/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flightJson(180)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flightNumber").value("W43381"));

        verify(flightService).createFlight(any(FlightRequest.class));
    }

    @Test
    void getFlights_usesDefaultPageAndSize() throws Exception {
        PageResponse<FlightResponse> response = new PageResponse<>(
            List.of(flightResponse()),
            0,
            10,
            1L,
            1
        );

        when(flightService.getFlights("CLJ", 0, 10))
            .thenReturn(response);

        mockMvc.perform(get("/flights")
                .param("airportCode", "CLJ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].flightNumber").value("W43381"))
            .andExpect(jsonPath("$.content[0].departureAirport").value("CLJ"))
            .andExpect(jsonPath("$.content[0].arrivalAirport").value("FCO"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));

        verify(flightService).getFlights("CLJ", 0, 10);
    }

    @Test
    void getFlights_usesRequestedPageAndSize() throws Exception {
        PageResponse<FlightResponse> response = new PageResponse<>(
            List.of(flightResponse()),
            1,
            5,
            6L,
            2
        );

        when(flightService.getFlights("CLJ", 1, 5))
            .thenReturn(response);

        mockMvc.perform(get("/flights")
                .param("airportCode", "CLJ")
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].flightNumber").value("W43381"))
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(5))
            .andExpect(jsonPath("$.totalElements").value(6))
            .andExpect(jsonPath("$.totalPages").value(2));

        verify(flightService).getFlights("CLJ", 1, 5);
    }

    @Test
    void getFlights_invalidSize_returnsBadRequest() throws Exception {
        when(flightService.getFlights("CLJ", 0, 7))
            .thenThrow(new IllegalArgumentException(
                "size must be one of: 5, 10, 20, 50, 100"
            ));

        mockMvc.perform(get("/flights")
                .param("airportCode", "CLJ")
                .param("size", "7"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "size must be one of: 5, 10, 20, 50, 100"
            ));

        verify(flightService).getFlights("CLJ", 0, 7);
    }

    @Test
    void getFlights_negativePage_returnsBadRequest() throws Exception {
        when(flightService.getFlights("CLJ", -1, 10))
            .thenThrow(new IllegalArgumentException(
                "page must be greater than or equal to 0"
            ));

        mockMvc.perform(get("/flights")
                .param("airportCode", "CLJ")
                .param("page", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "page must be greater than or equal to 0"
            ));

        verify(flightService).getFlights("CLJ", -1, 10);
    }

    @Test
    void getFlights_nonNumericPage_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/flights")
                .param("airportCode", "CLJ")
                .param("page", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "page must be a valid integer"
            ));

        verifyNoInteractions(flightService);
    }

    @Test
    void getFlights_nonNumericSize_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/flights")
                .param("airportCode", "CLJ")
                .param("size", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "size must be a valid integer"
            ));

        verifyNoInteractions(flightService);
    }

    @Test
    void getFlights_pageBeyondEnd_returnsEmptyContent() throws Exception {
        PageResponse<FlightResponse> response = new PageResponse<>(
            List.of(),
            3,
            10,
            25L,
            3
        );

        when(flightService.getFlights("CLJ", 3, 10))
            .thenReturn(response);

        mockMvc.perform(get("/flights")
                .param("airportCode", "CLJ")
                .param("page", "3")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.page").value(3))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.totalPages").value(3));

        verify(flightService).getFlights("CLJ", 3, 10);
    }

    @Test
    void getFlights_missingAirportCode_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/flights"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(flightService);
    }

    @Test
    void updateFlight_returnsUpdatedFlight() throws Exception {
        when(flightService.updateFlight(any(FlightRequest.class)))
            .thenReturn(flight(200));

        mockMvc.perform(put("/flights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(flightJson(200)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flightNumber").value("W43381"))
            .andExpect(jsonPath("$.numberOfSeats").value(200));

        verify(flightService).updateFlight(any(FlightRequest.class));
    }

    @Test
    void deleteFlight_callsService() throws Exception {
        mockMvc.perform(delete("/flights")
                .param("flightNumber", "W43381"))
            .andExpect(status().isOk());

        verify(flightService).deleteFlight("W43381");
    }

    private FlightResponse flightResponse() {
        return new FlightResponse(
            "W43381",
            "CLJ",
            "FCO",
            Instant.parse("2026-10-10T08:00:00Z")
                .atZone(ZoneId.systemDefault()),
            Instant.parse("2026-10-10T10:00:00Z")
                .atZone(ZoneId.systemDefault())
        );
    }

    private Flight flight(Integer seats) {
        Flight flight = new Flight();
        flight.setFlightNumber("W43381");
        flight.setDepartureAirportCode("CLJ");
        flight.setArrivalAirportCode("FCO");
        flight.setDepartureTime(Instant.parse("2026-10-10T08:00:00Z"));
        flight.setArrivalTime(Instant.parse("2026-10-10T10:00:00Z"));
        flight.setNumberOfSeats(seats);
        return flight;
    }

    private String flightJson(Integer seats) {
        return """
            {
              "flightNumber": "W43381",
              "departureAirportCode": "CLJ",
              "arrivalAirportCode": "FCO",
              "departureTime": "2026-10-10T08:00:00Z",
              "arrivalTime": "2026-10-10T10:00:00Z",
              "numberOfSeats": %d
            }
            """.formatted(seats);
    }
}
