package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.TripAccommodationRequest;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class TripAccommodationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        accommodationRepository.deleteAll();
    }

    @Test
    void addListAndRemoveAccommodation_shouldKeepAccommodationInCatalog()
        throws Exception {

        String username = "test_" + UUID.randomUUID().toString().substring(0, 8);

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user = userRepository.save(user);

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setDestination("Cluj");
        trip.setStartDate(LocalDate.of(2026, 6, 10));
        trip.setEndDate(LocalDate.of(2026, 6, 15));
        trip = tripRepository.save(trip);

        Accommodation accommodation = new Accommodation();
        accommodation.setName("Hotel Central");
        accommodation.setAddress("Str. Memorandumului 10");
        accommodation.setCity("Cluj");
        accommodation.setPricePerNight(BigDecimal.valueOf(250));
        accommodation = accommodationRepository.save(accommodation);

        TripAccommodationRequest request = new TripAccommodationRequest(
            username,
            accommodation.getId()
        );

        mockMvc.perform(
                post("/trips/" + trip.getId() + "/accommodations")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(content().string(""));

        mockMvc.perform(
                get("/trips/" + trip.getId() + "/accommodations")
                    .param("username", username)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id")
                .value(accommodation.getId().toString()))
            .andExpect(jsonPath("$.content[0].name").value("Hotel Central"))
            .andExpect(jsonPath("$.content[0].address")
                .value("Str. Memorandumului 10"))
            .andExpect(jsonPath("$.content[0].city").value("Cluj"))
            .andExpect(jsonPath("$.content[0].pricePerNight").value(250))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(
                get("/trips/" + trip.getId() + "/accommodations")
                    .param("username", username)
                    .param("page", "1")
                    .param("size", "10")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(
                delete(
                    "/trips/" + trip.getId()
                        + "/accommodations/" + accommodation.getId()
                )
                    .with(csrf())
                    .param("username", username)
            )
            .andExpect(status().isOk());

        mockMvc.perform(
                get("/trips/" + trip.getId() + "/accommodations")
                    .param("username", username)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.totalPages").value(0));

        assertTrue(
            accommodationRepository.findById(accommodation.getId()).isPresent()
        );
    }
}
