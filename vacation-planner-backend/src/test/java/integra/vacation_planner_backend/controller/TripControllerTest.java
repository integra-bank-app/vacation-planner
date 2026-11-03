package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.dto.TripResponse;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.exception.UserNotFoundException;
import integra.vacation_planner_backend.service.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import integra.vacation_planner_backend.dto.TripAccommodationRequest;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.ForbiddenException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.dto.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

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
            .andExpect(jsonPath("$.error").value("username is required"));
    }

    @Test
    void createTrip_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        TripDTO dto = new TripDTO();
        dto.setUsername("unknown");
        dto.setDestination("Paris");
        dto.setStartDate(LocalDate.of(2026, 6, 10));
        dto.setEndDate(LocalDate.of(2026, 6, 15));

        when(tripService.createTrip(any(TripDTO.class)))
            .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/trips")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    void getTrips_shouldReturnTrips() throws Exception {
        TripResponse trip = new TripResponse(
            UUID.randomUUID(),
            "bob",
            "Paris",
            LocalDate.of(2026, 6, 10),
            LocalDate.of(2026, 6, 15)
        );

        when(tripService.getTripsBetweenDates(
            "bob",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            0,
            10
        )).thenReturn(new PageResponse<>(
            List.of(trip), 0, 10, 1L, 1
        ));

        mockMvc.perform(get("/trips")
                .param("username", "bob")
                .param("startDate", "2026-06-01")
                .param("endDate", "2026-06-30"))
        searchTrips(user, "2027-06-10", "2027-06-15")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].destination").value("Paris"));
            .andExpect(jsonPath("$.content[0].id").value(trip.id().toString()))
            .andExpect(jsonPath("$.content[0].username").value("bob"))
            .andExpect(jsonPath("$.content[0].destination").value("Paris"))
            .andExpect(jsonPath("$.content[0].startDate").value("2026-06-10"))
            .andExpect(jsonPath("$.content[0].endDate").value("2026-06-15"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));

        verify(tripService).getTripsBetweenDates(
            "bob",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            0,
            10
        );
    }

    @Test
    void getTrips_excludesATripThatStartsBeforeTheRange() throws Exception {
        String user = userWithThreeTrips();
    void getTrips_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        when(tripService.getTripsBetweenDates(
            eq("unknown"),
            any(LocalDate.class),
            any(LocalDate.class),
            eq(0),
            eq(10)
        )).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/trips")
                .param("username", "unknown")
                .param("startDate", "2026-06-01")
                .param("endDate", "2026-06-30"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("User not found"));
    }

    @Test
    void getTrips_excludesATripThatEndsAfterTheRange() throws Exception {
        String user = userWithThreeTrips();
    void getTrips_shouldReturnEmptyPage_whenNoTripsExist() throws Exception {
        when(tripService.getTripsBetweenDates(
            "bob",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            0,
            10
        )).thenReturn(new PageResponse<TripResponse>(
            List.of(), 0, 10, 0L, 0
        ));

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
            any(LocalDate.class),
            eq(0),
            eq(10)
        )).thenThrow(new IllegalArgumentException(
            "startDate must be before endDate"
        ));
    // ------------------------------------------------------------------
    // PUT /trips/{id}
    // ------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("validUpdates")
    void updateTrip_changesOnlyTheDatesSent(
        LocalDate newStart, LocalDate newEnd, LocalDate expectedStart, LocalDate expectedEnd
    ) throws Exception {
        String user = user("erin");
        UUID tripId = createTrip(user, "Paris", START, END);

        putTrip(tripId, tripDto(user, null, newStart, newEnd)).andExpect(status().isOk());

        assertThat(tripRepository.findById(tripId)).hasValueSatisfying(trip -> {
            assertThat(trip.getStartDate()).isEqualTo(expectedStart);
            assertThat(trip.getEndDate()).isEqualTo(expectedEnd);
        });
    }

        mockMvc.perform(get("/trips")
                .param("username", "bob")
                .param("startDate", "2026-06-30")
                .param("endDate", "2026-06-01"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value(
                "startDate must be before endDate"
            ));
            .andExpect(jsonPath("$.error").value(message));

        assertThat(tripRepository.findById(tripId)).hasValueSatisfying(trip -> {
            assertThat(trip.getStartDate()).isEqualTo(START);
            assertThat(trip.getEndDate()).isEqualTo(END);
        });
    }

    static Stream<Arguments> invalidUpdates() {
        return Stream.of(
            Arguments.of(null, null, "At least one date must be provided"),
            // new start is after the stored end
            Arguments.of(LocalDate.of(2027, 6, 20), null, DATE_ORDER_ERROR),
            // new end is before the stored start
            Arguments.of(null, LocalDate.of(2027, 6, 5), DATE_ORDER_ERROR),
            Arguments.of(LocalDate.of(2027, 7, 5), LocalDate.of(2027, 7, 1), DATE_ORDER_ERROR)
        );
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
            .andExpect(jsonPath("$.error").value("At least one date must be provided"));
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

    @Test
    void addAccommodationToTrip_shouldReturnOk() throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        TripAccommodationRequest request = new TripAccommodationRequest(
            "bob",
            accommodationId
        );

        mockMvc.perform(post("/trips/" + tripId + "/accommodations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

           verify(tripService).addAccommodationToTrip(eq(tripId), any(TripAccommodationRequest.class));
    }

    @Test
    void addAccommodationToTrip_shouldReturnNotFound_whenTripOrAccommodationDoesNotExist()
        throws Exception {

        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        TripAccommodationRequest request = new TripAccommodationRequest(
            "bob",
            accommodationId
        );

        doThrow(new NotFoundException("Trip not found"))
            .when(tripService)
            .addAccommodationToTrip(eq(tripId), any(TripAccommodationRequest.class));

        mockMvc.perform(post("/trips/" + tripId + "/accommodations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addAccommodationToTrip_shouldReturnForbidden_whenUserIsNotOwner()
        throws Exception {

           UUID tripId = UUID.randomUUID();
           UUID accommodationId = UUID.randomUUID();

        TripAccommodationRequest request = new TripAccommodationRequest(
            "alice",
            accommodationId
        );

        doThrow(new ForbiddenException("User is not the owner of this trip"))
            .when(tripService)
            .addAccommodationToTrip(eq(tripId), any(TripAccommodationRequest.class));

        mockMvc.perform(post("/trips/" + tripId + "/accommodations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addAccommodationToTrip_shouldReturnConflict_whenAccommodationAlreadyAdded()
        throws Exception {

        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        TripAccommodationRequest request = new TripAccommodationRequest(
            "bob",
            accommodationId
        );

        doThrow(new ConflictException("Accommodation already added to trip"))
            .when(tripService)
            .addAccommodationToTrip(eq(tripId), any(TripAccommodationRequest.class));

        mockMvc.perform(post("/trips/" + tripId + "/accommodations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getTripAccommodations_shouldReturnAccommodations() throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        TripAccommodationResponse accommodation = new TripAccommodationResponse(
            accommodationId,
            "Hotel Central",
            "Str. Memorandumului 10",
            "Cluj",
            BigDecimal.valueOf(250)
        );
        getAccommodations(tripId, owner)
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        when(tripService.getTripAccommodations(tripId, "bob", 0, 10))
            .thenReturn(new PageResponse<>(
                List.of(accommodation), 0, 10, 1L, 1
            ));
        addAccommodation(tripId, owner, hotel.getId()).andExpect(status().isOk());

        mockMvc.perform(get("/trips/" + tripId + "/accommodations")
                .param("username", "bob"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(accommodationId.toString()))
            .andExpect(jsonPath("$.content[0].name").value("Hotel Central"))
            .andExpect(jsonPath("$.content[0].address").value("Str. Memorandumului 10"))
            .andExpect(jsonPath("$.content[0].city").value("Cluj"))
            .andExpect(jsonPath("$.content[0].pricePerNight").value(250))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(hotel.getId().toString()))
            .andExpect(jsonPath("$[0].name").value("Hotel Central"))
            .andExpect(jsonPath("$[0].address").value("Str. Memorandumului 10"))
            .andExpect(jsonPath("$[0].city").value("Cluj"))
            .andExpect(jsonPath("$[0].pricePerNight").value(250));

        removeAccommodation(tripId, hotel.getId(), owner).andExpect(status().isOk());

        getAccommodations(tripId, owner).andExpect(content().json("[]"));
        // the accommodation itself is kept
        assertThat(accommodationRepository.existsById(hotel.getId())).isTrue();
        verify(tripService).getTripAccommodations(tripId, "bob", 0, 10);
    }

    @Test
    void getTripAccommodations_shouldReturnForbidden_whenUserIsNotOwner()
        throws Exception {

        UUID tripId = UUID.randomUUID();

        when(tripService.getTripAccommodations(tripId, "alice", 0, 10))
            .thenThrow(new ForbiddenException("User is not the owner of this trip"));
    @Test
    void addAccommodation_allowsTheSameAccommodationOnAnotherTrip() throws Exception {
        String owner = user("lena");
        UUID firstTrip = createTrip(owner, "Cluj", START, END);
        UUID secondTrip = createTrip(owner, "Cluj", START, END);
        Accommodation hotel = saveHotel();

        mockMvc.perform(get("/trips/" + tripId + "/accommodations")
                .param("username", "alice"))
            .andExpect(status().isForbidden());
        addAccommodation(firstTrip, owner, hotel.getId()).andExpect(status().isOk());
        addAccommodation(secondTrip, owner, hotel.getId()).andExpect(status().isOk());
    }

    @Test
    void addAccommodation_returnsForbiddenForANonOwner_andAddsNothing() throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);
        Accommodation hotel = saveHotel();

        addAccommodation(tripId, user("mallory"), hotel.getId())
            .andExpect(status().isForbidden());

        getAccommodations(tripId, owner).andExpect(content().json("[]"));
    }

    @Test
    void addAccommodation_returnsNotFoundForAnUnknownTrip() throws Exception {
        Accommodation hotel = saveHotel();

        addAccommodation(UUID.randomUUID(), VALID_USER, hotel.getId())
            .andExpect(status().isNotFound());
    }

    @Test
    void removeAccommodationFromTrip_shouldReturnOk() throws Exception {
        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        mockMvc.perform(delete("/trips/" + tripId + "/accommodations/" + accommodationId)
                .with(csrf())
                .param("username", "bob"))
                .andExpect(status().isOk());

        verify(tripService).removeAccommodationFromTrip(tripId, accommodationId, "bob");
    }

    @Test
    void removeAccommodationFromTrip_shouldReturnNotFound_whenAccommodationIsNotInTrip()
        throws Exception {

        UUID tripId = UUID.randomUUID();
        UUID accommodationId = UUID.randomUUID();

        doThrow(new NotFoundException("Accommodation not found in trip"))
            .when(tripService)
            .removeAccommodationFromTrip(tripId, accommodationId, "bob");

        mockMvc.perform(delete("/trips/" + tripId + "/accommodations/" + accommodationId)
                .with(csrf())
                .param("username", "bob"))
                .andExpect(status().isNotFound());
    }

    @Test
    void tripLists_shouldUseRequestedPaginationAndReturnEmptyPageBeyondEnd()
        throws Exception {

        UUID tripId = UUID.randomUUID();

        when(tripService.getTripsBetweenDates(
            "bob",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            3,
            5
        )).thenReturn(new PageResponse<TripResponse>(
            List.of(), 3, 5, 12L, 3
        ));

        when(tripService.getTripAccommodations(tripId, "bob", 3, 5))
            .thenReturn(new PageResponse<TripAccommodationResponse>(
                List.of(), 3, 5, 12L, 3
            ));

        for (String path : List.of("/trips", "/trips/" + tripId + "/accommodations")) {
            mockMvc.perform(tripListRequest(path)
                    .param("page", "3")
                    .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page").value(3))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3));
        }

        verify(tripService).getTripsBetweenDates(
            "bob",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            3,
            5
        );
        verify(tripService).getTripAccommodations(tripId, "bob", 3, 5);
    }

    @Test
    void tripLists_shouldRejectInvalidSize() throws Exception {
        UUID tripId = UUID.randomUUID();
        String message = "size must be one of: 5, 10, 20, 50, 100";

        when(tripService.getTripsBetweenDates(
            "bob",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            0,
            7
        )).thenThrow(new IllegalArgumentException(message));

        when(tripService.getTripAccommodations(tripId, "bob", 0, 7))
            .thenThrow(new IllegalArgumentException(message));

        for (String path : List.of("/trips", "/trips/" + tripId + "/accommodations")) {
            mockMvc.perform(tripListRequest(path).param("size", "7"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(message));
        }
    }

    @Test
    void tripLists_shouldRejectNegativePage() throws Exception {
        UUID tripId = UUID.randomUUID();
        String message = "page must be greater than or equal to 0";

        when(tripService.getTripsBetweenDates(
            "bob",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            -1,
            10
        )).thenThrow(new IllegalArgumentException(message));

        when(tripService.getTripAccommodations(tripId, "bob", -1, 10))
            .thenThrow(new IllegalArgumentException(message));

        for (String path : List.of("/trips", "/trips/" + tripId + "/accommodations")) {
            mockMvc.perform(tripListRequest(path).param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(message));
        }
    }

    @Test
    void tripLists_shouldRejectNonNumericPagination() throws Exception {
        UUID tripId = UUID.randomUUID();

        for (String path : List.of("/trips", "/trips/" + tripId + "/accommodations")) {
            for (String parameter : List.of("page", "size")) {
                mockMvc.perform(tripListRequest(path).param(parameter, "abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(
                        parameter + " must be a valid integer"
                    ));
            }
        }

        org.mockito.Mockito.verifyNoInteractions(tripService);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
    tripListRequest(String path) {

        return get(path)
            .param("username", "bob")
            .param("startDate", "2026-06-01")
            .param("endDate", "2026-06-30");
    }

    @Test
    void removeAccommodation_returnsNotFoundForAnUnknownAccommodation() throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);

        removeAccommodation(tripId, UUID.randomUUID(), owner).andExpect(status().isNotFound());
    }

    @Test
    void removeAccommodation_returnsNotFoundForAnAccommodationThatIsNotOnTheTrip()
        throws Exception {
        String owner = user("lena");
        UUID tripId = createTrip(owner, "Cluj", START, END);
        Accommodation notOnTrip = saveHotel();

        removeAccommodation(tripId, notOnTrip.getId(), owner).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Requests Spring rejects before the service is reached
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0}")
    @MethodSource("malformedRequests")
    void malformedRequests_areRejectedWithBadRequest(String description, RequestBuilder request)
        throws Exception {
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    static Stream<Arguments> malformedRequests() {
        MediaType json = MediaType.APPLICATION_JSON;
        return Stream.of(
            Arguments.of("POST /trips with invalid JSON",
                post("/trips").contentType(json).content("{ this is not json")),
            Arguments.of("POST /trips with no body",
                post("/trips").contentType(json)),
            Arguments.of("POST /trips with an impossible date",
                post("/trips").contentType(json).content(
                    "{\"username\":\"bob\",\"destination\":\"Paris\","
                        + "\"startDate\":\"2027-13-45\",\"endDate\":\"2027-06-15\"}")),
            Arguments.of("PUT /trips/{id} with an invalid trip id",
                put("/trips/not-a-uuid").contentType(json).content("{}")),
            Arguments.of("POST /trips/{id}/flights with an invalid trip id",
                post("/trips/not-a-uuid/flights").contentType(json).content("{}")),
            Arguments.of("GET /trips/{id}/flights with an invalid trip id",
                get("/trips/not-a-uuid/flights").param("username", "bob")),
            Arguments.of("GET /trips/{id}/flights without username",
                get("/trips/" + UUID.randomUUID() + "/flights")),
            Arguments.of("DELETE /trips/{id} with an invalid trip id",
                delete("/trips/not-a-uuid").param("username", "bob")),
            Arguments.of("DELETE /trips/{id} without username",
                delete("/trips/" + UUID.randomUUID())),
            Arguments.of("DELETE /trips/{id}/flights/{n} with an invalid trip id",
                delete("/trips/not-a-uuid/flights/RO101").param("username", "bob")),
            Arguments.of("GET /trips without username",
                get("/trips").param("startDate", "2027-06-01").param("endDate", "2027-06-30")),
            Arguments.of("GET /trips without startDate",
                get("/trips").param("username", "bob").param("endDate", "2027-06-30")),
            Arguments.of("GET /trips without endDate",
                get("/trips").param("username", "bob").param("startDate", "2027-06-01")),
            Arguments.of("GET /trips with a startDate that is not a date",
                get("/trips").param("username", "bob").param("startDate", "not-a-date")
                    .param("endDate", "2027-06-30"))
        );
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /** A user with trips in Jun, Jul and Aug 2027, plus an unrelated user's trip in Jun. */
    private String userWithThreeTrips() throws Exception {
        String user = user("carol");
        createTrip(user, "Paris", LocalDate.of(2027, 6, 10), LocalDate.of(2027, 6, 15));
        createTrip(user, "Rome", LocalDate.of(2027, 7, 1), LocalDate.of(2027, 7, 5));
        createTrip(user, "Oslo", LocalDate.of(2027, 8, 10), LocalDate.of(2027, 8, 20));
        createTrip(user("dave"), "Madrid", LocalDate.of(2027, 6, 10), LocalDate.of(2027, 6, 15));
        return user;
    }

    private Accommodation saveHotel() {
        return saveAccommodation("Hotel Central", "Str. Memorandumului 10", "Cluj");
    }

    private UUID createTrip(String username, String destination, LocalDate start, LocalDate end)
        throws Exception {
        MvcResult result = postTrip(tripDto(username, destination, start, end))
            .andExpect(status().isOk())
            .andReturn();

        UUID id = objectMapper.readValue(result.getResponse().getContentAsString(), UUID.class);
        createdTripIds.add(id);
        return id;
    }

    private ResultActions postTrip(TripDTO dto) throws Exception {
        return mockMvc.perform(post("/trips")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions putTrip(UUID tripId, TripDTO dto) throws Exception {
        return mockMvc.perform(put("/trips/" + tripId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions deleteTrip(UUID tripId, String username) throws Exception {
        return mockMvc.perform(delete("/trips/" + tripId).param("username", username));
    }

    private ResultActions searchTrips(String username, String startDate, String endDate)
        throws Exception {
        return mockMvc.perform(get("/trips")
            .param("username", username)
            .param("startDate", startDate)
            .param("endDate", endDate));
    }

    private ResultActions addFlight(UUID tripId, String username, String flightNumber)
        throws Exception {
        AddFlightToTripRequest request = new AddFlightToTripRequest();
        request.setUsername(username);
        request.setFlightNumber(flightNumber);

        return mockMvc.perform(post("/trips/" + tripId + "/flights")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions getFlights(UUID tripId, String username) throws Exception {
        return mockMvc.perform(get("/trips/" + tripId + "/flights").param("username", username));
    }

    private ResultActions removeFlight(UUID tripId, String flightNumber, String username)
        throws Exception {
        return mockMvc.perform(delete("/trips/" + tripId + "/flights/" + flightNumber)
            .param("username", username));
    }

    private Flight saveFlight(String flightNumber, Instant departure, Instant arrival, int seats) {
        Flight flight = new Flight();
        flight.setFlightNumber(flightNumber);
        flight.setDepartureAirportCode("CLJ");
        flight.setArrivalAirportCode("CDG");
        flight.setDepartureTime(departure);
        flight.setArrivalTime(arrival);
        flight.setNumberOfSeats(seats);
        flight.setPrice(new BigDecimal("150.00"));

        Flight saved = flightRepository.save(flight);
        createdFlightIds.add(saved.getId());
        return saved;
    }

    private static TripDTO tripDto(
        String username, String destination, LocalDate start, LocalDate end
    ) {
        TripDTO dto = new TripDTO();
        dto.setUsername(username);
        dto.setDestination(destination);
        dto.setStartDate(start);
        dto.setEndDate(end);
        return dto;
    }

    /** Flight times are serialised in the server's time zone, so compare instants, not text. */
    private static Instant instantAt(String json, String path) {
        String value = JsonPath.read(json, path);
        return ZonedDateTime.parse(value).toInstant();
    }

    private ResultActions addAccommodation(UUID tripId, String username, UUID accommodationId)
        throws Exception {
        return mockMvc.perform(post("/trips/" + tripId + "/accommodations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new TripAccommodationRequest(username, accommodationId))));
    }

    private ResultActions getAccommodations(UUID tripId, String username) throws Exception {
        return mockMvc.perform(get("/trips/" + tripId + "/accommodations")
            .param("username", username));
    }

    private ResultActions removeAccommodation(UUID tripId, UUID accommodationId, String username)
        throws Exception {
        return mockMvc.perform(delete("/trips/" + tripId + "/accommodations/" + accommodationId)
            .param("username", username));
    }

    private Accommodation saveAccommodation(String name, String address, String city) {
        Accommodation saved = accommodationRepository.save(
            new Accommodation(name, BigDecimal.valueOf(250), address, city));
        createdAccommodationIds.add(saved.getId());
        return saved;
    }

    private void createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.local");
        user.setFirstName("Test");
        user.setLastName("User");
        createdUserIds.add(userRepository.save(user).getId());
    }

    /** Creates a real user with a unique name; the API rejects trips for unknown users. */
    private String user(String prefix) {
        String username = unique(prefix);
        createUser(username);
        return username;
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
