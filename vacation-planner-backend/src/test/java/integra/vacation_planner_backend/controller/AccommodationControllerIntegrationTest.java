package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.AccommodationRequest;
import integra.vacation_planner_backend.dto.UpdatePriceRequest;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import integra.vacation_planner_backend.repository.TripRepository;
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

    @Autowired
    private TripRepository tripRepository;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        repository.deleteAll();
    }

    @Test
    void findByCity_shouldUseRequestedPageAndSize() throws Exception {
        for (int index = 5; index >= 0; index--) {
            createAccommodation(
                "Hotel %02d".formatted(index),
                BigDecimal.valueOf(100 + index),
                "Str. Test 1",
                "Cluj"
            );
        }

        mockMvc.perform(get("/accommodations")
                .param("city", "Cluj")
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Hotel 05"))
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(5))
            .andExpect(jsonPath("$.totalElements").value(6))
            .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void findByCity_shouldReturnBadRequest_whenSizeIsInvalid() throws Exception {
        mockMvc.perform(get("/accommodations")
                .param("city", "Cluj")
                .param("size", "7"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "size must be one of: 5, 10, 20, 50, 100"
            ));
    }

    @Test
    void findByCity_shouldReturnBadRequest_whenPageIsNegative() throws Exception {
        mockMvc.perform(get("/accommodations")
                .param("city", "Cluj")
                .param("page", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "page must be greater than or equal to 0"
            ));
    }

    @Test
    void findByCity_shouldReturnBadRequest_whenPageIsNotANumber() throws Exception {
        mockMvc.perform(get("/accommodations")
                .param("city", "Cluj")
                .param("page", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "page must be a valid integer"
            ));
    }

    @Test
    void findByCity_shouldReturnBadRequest_whenSizeIsNotANumber() throws Exception {
        mockMvc.perform(get("/accommodations")
                .param("city", "Cluj")
                .param("size", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "size must be a valid integer"
            ));
    }

    @Test
    void findByCity_shouldReturnEmptyContent_whenPageIsBeyondEnd() throws Exception {
        createAccommodation();

        mockMvc.perform(get("/accommodations")
                .param("city", "Cluj")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
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
    void findByCity_shouldReturnDefaultPageSortedByName() throws Exception {
        createAccommodation(
            "Zeta Hotel",
            BigDecimal.valueOf(100),
            "Str. A 1",
            "Cluj"
        );

        createAccommodation(
            "Alpha Hotel",
            BigDecimal.valueOf(500),
            "Str. B 2",
            "Cluj"
        );

        mockMvc.perform(get("/accommodations")
                .param("city", "Cluj"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].name").value("Alpha Hotel"))
            .andExpect(jsonPath("$.content[0].pricePerNight").value(500))
            .andExpect(jsonPath("$.content[1].name").value("Zeta Hotel"))
            .andExpect(jsonPath("$.content[1].pricePerNight").value(100))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void findByCity_shouldIgnoreCase() throws Exception {
        createAccommodation(
            "Hotel Central",
            BigDecimal.valueOf(250),
            "Str. Memorandumului 10",
            "Cluj"
        );

        mockMvc.perform(get("/accommodations")
                .param("city", "cluj"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Hotel Central"))
            .andExpect(jsonPath("$.totalElements").value(1));
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
