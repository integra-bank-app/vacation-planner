package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.dto.FlightRequest;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.exception.ValidationException;
import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    @Test
    void createFlight_validRequest_savesFlight() {
        FlightRequest request = validRequest();
        when(flightRepository.findByFlightNumber("W43381")).thenReturn(Optional.empty());
        flightService.createFlight(request);

        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository).save(captor.capture());
        assertEquals(new BigDecimal("150.00"), captor.getValue().getPrice());
    }

    @Test
    void createFlight_existingFlight_throwsConflictException() {
        FlightRequest request = validRequest();
        when(flightRepository.findByFlightNumber("W43381")).thenReturn(Optional.of(new Flight()));
        assertThrows(ConflictException.class, () -> flightService.createFlight(request));
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
        assertThrows(ValidationException.class, () -> flightService.createFlight(request));
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
        assertThrows(ValidationException.class, () -> flightService.createFlight(request));
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
        when(flightRepository.findByFlightNumber("W43381")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> flightService.updateFlight(request));
    }

    @Test
    void deleteFlight_missingFlight_throwsNotFoundException() {
        when(flightRepository.findByFlightNumber("W43381")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> flightService.deleteFlight("W43381"));
    }

    @Test
    void getFlights_blankAirportCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> flightService.getFlights(""));
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
}
