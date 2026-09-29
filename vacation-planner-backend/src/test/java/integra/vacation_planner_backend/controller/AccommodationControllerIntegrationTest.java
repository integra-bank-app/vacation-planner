package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.AccommodationRequest;
import integra.vacation_planner_backend.dto.UpdatePriceRequest;
import integra.vacation_planner_backend.repository.AccommodationRepository;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class AccommodationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccommodationRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void create_shouldReturnId() throws Exception {
        AccommodationRequest request = new AccommodationRequest(
                "Hotel Central",
                BigDecimal.valueOf(250),
                "Str. Memorandumului 10",
                "Cluj"
        );

        mockMvc.perform(
                        post("/accommodations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void updatePrice_shouldReturnOk() throws Exception {
        UUID id = createAccommodation();

        UpdatePriceRequest request = new UpdatePriceRequest(
                BigDecimal.valueOf(300)
        );

        mockMvc.perform(
                        put("/accommodations/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void updatePrice_shouldReturnBadRequest_whenPriceIsInvalid()
            throws Exception {

        UUID id = createAccommodation();

        UpdatePriceRequest request = new UpdatePriceRequest(
                BigDecimal.valueOf(-100)
        );

        mockMvc.perform(
                        put("/accommodations/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePrice_shouldReturnNotFound_whenAccommodationDoesNotExist()
            throws Exception {

        UUID id = UUID.randomUUID();

        UpdatePriceRequest request = new UpdatePriceRequest(
                BigDecimal.valueOf(300)
        );

        mockMvc.perform(
                        put("/accommodations/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturnNoContent() throws Exception {
        UUID id = createAccommodation();

        mockMvc.perform(
                        delete("/accommodations/" + id)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturnNotFound_whenAccommodationDoesNotExist()
            throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                        delete("/accommodations/" + id)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void findByCity_shouldReturnAccommodationsSortedByPrice()
            throws Exception {

        createAccommodation(
                "Expensive Hotel",
                BigDecimal.valueOf(500),
                "Str. A 1",
                "Cluj"
        );

        createAccommodation(
                "Cheap Hotel",
                BigDecimal.valueOf(200),
                "Str. B 2",
                "Cluj"
        );

        mockMvc.perform(
                        get("/accommodations")
                                .param("city", "Cluj")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Cheap Hotel"))
                .andExpect(jsonPath("$[0].pricePerNight").value(200))
                .andExpect(jsonPath("$[1].name").value("Expensive Hotel"))
                .andExpect(jsonPath("$[1].pricePerNight").value(500));
    }

    @Test
    void findByCity_shouldIgnoreCase() throws Exception {
        createAccommodation(
                "Hotel Central",
                BigDecimal.valueOf(250),
                "Str. Memorandumului 10",
                "Cluj"
        );

        mockMvc.perform(
                        get("/accommodations")
                                .param("city", "cluj")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Hotel Central"));
    }

    private UUID createAccommodation() throws Exception {
        return createAccommodation(
                "Hotel Central",
                BigDecimal.valueOf(250),
                "Str. Memorandumului 10",
                "Cluj"
        );
    }

    private UUID createAccommodation(
            String name,
            BigDecimal price,
            String address,
            String city
    ) throws Exception {

        AccommodationRequest request = new AccommodationRequest(
                name,
                price,
                address,
                city
        );

        String response = mockMvc.perform(
                        post("/accommodations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(
                objectMapper.readTree(response).asText()
        );
    }
}