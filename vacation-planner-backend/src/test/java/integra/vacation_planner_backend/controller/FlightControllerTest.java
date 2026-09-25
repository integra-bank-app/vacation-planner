package integra.vacation_planner_backend.controller;
import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.domain.Flight;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlightService flightService;

    @Test
    void createFlight_returnsCreatedFlight() throws Exception {
        when(flightService.createFlight(any(FlightRequest.class))).thenReturn(flight(180));
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightJson(180)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("W43381"));

        verify(flightService).createFlight(any(FlightRequest.class));
    }

    @Test
    void getFlights_returnsFlights() throws Exception {
        FlightResponse response = new FlightResponse(
                "W43381",
                "CLJ",
                "FCO",
                Instant.parse("2026-10-10T08:00:00Z").atZone(ZoneId.systemDefault()),
                Instant.parse("2026-10-10T10:00:00Z").atZone(ZoneId.systemDefault())
        );
        when(flightService.getFlights("CLJ")).thenReturn(List.of(response));

        mockMvc.perform(get("/flights").param("airportCode", "CLJ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber").value("W43381"))
                .andExpect(jsonPath("$[0].departureAirport").value("CLJ"))
                .andExpect(jsonPath("$[0].arrivalAirport").value("FCO"));
        verify(flightService).getFlights("CLJ");
    }

    @Test
    void updateFlight_returnsUpdatedFlight() throws Exception {
        when(flightService.updateFlight(any(FlightRequest.class))).thenReturn(flight(200));
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
        mockMvc.perform(delete("/flights").param("flightNumber", "W43381"))
                .andExpect(status().isOk());
        verify(flightService).deleteFlight("W43381");
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