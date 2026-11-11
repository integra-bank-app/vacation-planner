package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.dto.PageResponse;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.exception.ValidationException;
import integra.vacation_planner_backend.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    @Mock
    private RandomGenerator randomGenerator;

    @Test
    void createFlight_validRequest_savesFlight() {
        FlightRequest request = validRequest();

        when(flightRepository.findByFlightNumber("W43381"))
            .thenReturn(Optional.empty());

        flightService.createFlight(request);

        verify(flightRepository).save(any(Flight.class));

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository).save(captor.capture());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getPrice());
    }

    @Test
    void createFlight_existingFlight_throwsConflictException() {
        FlightRequest request = validRequest();

        when(flightRepository.findByFlightNumber("W43381"))
            .thenReturn(Optional.of(new Flight()));

        assertThrows(
            ConflictException.class,
            () -> flightService.createFlight(request)
        );

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void createFlight_invalidSeats_throwsValidationException() {
        FlightRequest request = new FlightRequest(
            "W43381",
            "CLJ",
            "FCO",
            Instant.parse("2026-10-10T08:00:00Z"),
            Instant.parse("2026-10-10T10:00:00Z"),
            0,
            new BigDecimal("150.00")
        );

        assertThrows(
            ValidationException.class,
            () -> flightService.createFlight(request)
        );
    }

    @Test
    void createFlight_arrivalBeforeDeparture_throwsValidationException() {
        FlightRequest request = new FlightRequest(
            "W43381",
            "CLJ",
            "FCO",
            Instant.parse("2026-10-10T10:00:00Z"),
            Instant.parse("2026-10-10T08:00:00Z"),
            180,
            new BigDecimal("150.00")
        );

        assertThrows(
            ValidationException.class,
            () -> flightService.createFlight(request)
        );
    }

    @Test
    void createFlight_missingPrice_throwsValidationException() {
        FlightRequest request = requestWithPrice(null);
        assertThrows(ValidationException.class, () -> flightService.createFlight(request));
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void createFlight_zeroPrice_throwsValidationException() {
        FlightRequest request = requestWithPrice(BigDecimal.ZERO);
        assertThrows(ValidationException.class, () -> flightService.createFlight(request));
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void createFlight_negativePrice_throwsValidationException() {
        FlightRequest request = requestWithPrice(new BigDecimal("-10.00"));
        assertThrows(ValidationException.class, () -> flightService.createFlight(request));
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void updateFlight_newPrice_savesFlightWithNewPrice() {
        Flight existing = new Flight();
        existing.setFlightNumber("W43381");
        existing.setPrice(new BigDecimal("150.00"));
        when(flightRepository.findByFlightNumber("W43381")).thenReturn(Optional.of(existing));

        flightService.updateFlight(requestWithPrice(new BigDecimal("199.99")));

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository).save(captor.capture());
        assertEquals(new BigDecimal("199.99"), captor.getValue().getPrice());
    }

    @Test
    void updateFlight_missingFlight_throwsNotFoundException() {
        FlightRequest request = validRequest();

        when(flightRepository.findByFlightNumber("W43381"))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> flightService.updateFlight(request)
        );
    }

    @Test
    void deleteFlight_missingFlight_throwsNotFoundException() {
        when(flightRepository.findByFlightNumber("W43381"))
            .thenReturn(Optional.empty());

        assertThrows(
            NotFoundException.class,
            () -> flightService.deleteFlight("W43381")
        );
    }

    @Test
    void getFlights_blankAirportCode_throwsValidationException() {
        assertThrows(
            ValidationException.class,
            () -> flightService.getFlights("", 0, 10)
        );

        verifyNoInteractions(flightRepository);
    }

    @Test
    void getFlights_usesRequestedPageAndStableSort() {
        Flight flight = validFlight();
        PageRequest expectedPageable = flightPageRequest(1, 10);

        when(flightRepository.findByDepartureAirportCodeOrArrivalAirportCode(
            eq("CLJ"),
            eq("CLJ"),
            any(Pageable.class)
        )).thenReturn(
            new PageImpl<>(List.of(flight), expectedPageable, 25)
        );

        PageResponse<FlightResponse> result =
            flightService.getFlights("CLJ", 1, 10);

        verify(flightRepository)
            .findByDepartureAirportCodeOrArrivalAirportCode(
                "CLJ",
                "CLJ",
                expectedPageable
            );

        assertEquals(1, result.page());
        assertEquals(10, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());

        assertEquals(
            List.of(new FlightResponse(
                flight.getFlightNumber(),
                flight.getDepartureAirportCode(),
                flight.getArrivalAirportCode(),
                flight.getDepartureTime().atZone(ZoneId.systemDefault()),
                flight.getArrivalTime().atZone(ZoneId.systemDefault()),
                flight.getPrice()
            )),
            result.content()
        );
    }

    @Test
    void getFlights_pageBeyondEnd_returnsEmptyContentAndKeepsTotals() {
        PageRequest pageable = flightPageRequest(3, 10);

        when(flightRepository.findByDepartureAirportCodeOrArrivalAirportCode(
            "CLJ",
            "CLJ",
            pageable
        )).thenReturn(
            new PageImpl<Flight>(List.of(), pageable, 25)
        );

        PageResponse<FlightResponse> result =
            flightService.getFlights("CLJ", 3, 10);

        assertTrue(result.content().isEmpty());
        assertEquals(3, result.page());
        assertEquals(10, result.size());
        assertEquals(25L, result.totalElements());
        assertEquals(3, result.totalPages());
    }

    @Test
    void getFlights_noMatchingFlights_returnsZeroTotals() {
        PageRequest pageable = flightPageRequest(0, 10);

        when(flightRepository.findByDepartureAirportCodeOrArrivalAirportCode(
            "CLJ",
            "CLJ",
            pageable
        )).thenReturn(
            new PageImpl<Flight>(List.of(), pageable, 0)
        );

        PageResponse<FlightResponse> result =
            flightService.getFlights("CLJ", 0, 10);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(0L, result.totalElements());
        assertEquals(0, result.totalPages());
    }

    @Test
    void getFlights_negativePage_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> flightService.getFlights("CLJ", -1, 10)
        );

        assertEquals(
            "page must be greater than or equal to 0",
            exception.getMessage()
        );

        verifyNoInteractions(flightRepository);
    }

    @Test
    void getFlights_invalidSizes_throwIllegalArgumentException() {
        for (int size : List.of(-1, 0, 1, 7, 15, 101)) {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> flightService.getFlights("CLJ", 0, size)
            );

            assertEquals(
                "size must be one of: 5, 10, 20, 50, 100",
                exception.getMessage()
            );
        }

        verifyNoInteractions(flightRepository);
    }

    @Test
    void getFlights_allowedSizes_arePassedToRepository() {
        for (int size : List.of(5, 10, 20, 50, 100)) {
            PageRequest pageable = flightPageRequest(0, size);

            when(flightRepository.findByDepartureAirportCodeOrArrivalAirportCode(
                "CLJ",
                "CLJ",
                pageable
            )).thenReturn(
                new PageImpl<Flight>(List.of(), pageable, 0)
            );

            PageResponse<FlightResponse> result =
                flightService.getFlights("CLJ", 0, size);

            assertEquals(size, result.size());

            verify(flightRepository)
                .findByDepartureAirportCodeOrArrivalAirportCode(
                    "CLJ",
                    "CLJ",
                    pageable
                );
        }
    }

    private PageRequest flightPageRequest(int page, int size) {
        return PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.asc("departureTime"),
                Sort.Order.asc("id")
            )
        );
    }

    private Flight validFlight() {
        FlightRequest request = validRequest();

        Flight flight = new Flight();
        flight.setId(UUID.randomUUID());
        flight.setFlightNumber(request.flightNumber());
        flight.setDepartureAirportCode(request.departureAirportCode());
        flight.setArrivalAirportCode(request.arrivalAirportCode());
        flight.setDepartureTime(request.departureTime());
        flight.setArrivalTime(request.arrivalTime());
        flight.setNumberOfSeats(request.numberOfSeats());

        return flight;
    }

    @Test
    void applyDailyChange_plusTenPercent_increasesPrice() {
        Flight flight = flightWithPrice("100.00");
        when(flightRepository.findByDepartureTimeAfter(any(Instant.class))).thenReturn(List.of(flight));
        when(randomGenerator.nextInt(anyInt(), anyInt())).thenReturn(10);

        flightService.applyDailyChange();

        assertEquals(new BigDecimal("110.00"), flight.getPrice());
    }

    @Test
    void applyDailyChange_minusTenPercent_decreasesPrice() {
        Flight flight = flightWithPrice("100.00");
        when(flightRepository.findByDepartureTimeAfter(any(Instant.class))).thenReturn(List.of(flight));
        when(randomGenerator.nextInt(anyInt(), anyInt())).thenReturn(-10);

        flightService.applyDailyChange();

        assertEquals(new BigDecimal("90.00"), flight.getPrice());
    }

    @Test
    void applyDailyChange_resultWithManyDecimals_roundsToTwoDecimals() {
        Flight flight = flightWithPrice("19.99");
        when(flightRepository.findByDepartureTimeAfter(any(Instant.class))).thenReturn(List.of(flight));
        when(randomGenerator.nextInt(anyInt(), anyInt())).thenReturn(7);

        flightService.applyDailyChange();

        assertEquals(new BigDecimal("21.39"), flight.getPrice());
    }

    @Test
    void applyDailyChange_priceBelowMinimum_keepsMinimumPrice() {
        Flight flight = flightWithPrice("1.00");
        when(flightRepository.findByDepartureTimeAfter(any(Instant.class))).thenReturn(List.of(flight));
        when(randomGenerator.nextInt(anyInt(), anyInt())).thenReturn(-10);

        flightService.applyDailyChange();

        assertEquals(new BigDecimal("1.00"), flight.getPrice());
    }

    @Test
    void applyDailyChange_multipleFlights_savesAllWithPercentInRange() {
        Flight first = flightWithPrice("100.00");
        Flight second = flightWithPrice("200.00");
        List<Flight> flights = List.of(first, second);
        when(flightRepository.findByDepartureTimeAfter(any(Instant.class))).thenReturn(flights);
        when(randomGenerator.nextInt(anyInt(), anyInt())).thenReturn(5);

        flightService.applyDailyChange();

        verify(randomGenerator, times(2)).nextInt(-10, 11);
        verify(flightRepository).saveAll(flights);
        assertEquals(new BigDecimal("105.00"), first.getPrice());
        assertEquals(new BigDecimal("210.00"), second.getPrice());
    }

    private FlightRequest validRequest() {
        return requestWithPrice(new BigDecimal("150.00"));
    }

    private FlightRequest requestWithPrice(BigDecimal price) {
        return new FlightRequest(
            "W43381",
            "CLJ",
            "FCO",
            Instant.parse("2026-10-10T08:00:00Z"),
            Instant.parse("2026-10-10T10:00:00Z"),
            180,
            price
        );
    }

    private Flight flightWithPrice(String price) {
        Flight flight = new Flight();
        flight.setPrice(new BigDecimal(price));
        return flight;
    }
}
