package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Transactional
class FlightPaginationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightRepository flightRepository;

    @Test
    void getFlights_returnsThreeOrderedPagesAndCorrectTotals() throws Exception {
        Instant departure = Instant.parse("2026-12-01T08:00:00Z");

        for (int index = 24; index >= 0; index--) {
            Flight flight = new Flight();
            flight.setFlightNumber("PAGE%02d".formatted(index));
            flight.setDepartureAirportCode("QZX");
            flight.setArrivalAirportCode("FCO");
            flight.setDepartureTime(
                departure.plus(index, ChronoUnit.HOURS)
            );
            flight.setArrivalTime(
                departure.plus(index + 2, ChronoUnit.HOURS)
            );
            flight.setNumberOfSeats(180);
            flightRepository.save(flight);
        }

        flightRepository.flush();

        for (int page = 0; page < 3; page++) {
            int expectedCount = page == 2 ? 5 : 10;

            var result = mockMvc.perform(get("/flights")
                    .param("airportCode", "QZX")
                    .param("page", Integer.toString(page))
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(expectedCount));

            for (int item = 0; item < expectedCount; item++) {
                String expectedFlightNumber =
                    "PAGE%02d".formatted(page * 10 + item);

                result.andExpect(
                    jsonPath("$.content[" + item + "].flightNumber")
                        .value(expectedFlightNumber)
                );
            }
        }

        mockMvc.perform(get("/flights")
                .param("airportCode", "QZX")
                .param("page", "3")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.page").value(3))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(25))
            .andExpect(jsonPath("$.totalPages").value(3));
    }
}
