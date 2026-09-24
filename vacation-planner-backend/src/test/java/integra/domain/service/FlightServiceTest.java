package integra.domain.service;
import integra.domain.dto.FlightRequest;
import integra.domain.exception.ConflictException;
import integra.domain.exception.NotFoundException;
import integra.domain.exception.ValidationException;
import integra.domain.model.Flight;
import integra.domain.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
        verify(flightRepository).save(any(Flight.class));
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
                0
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
                180
        );
        assertThrows(ValidationException.class, () -> flightService.createFlight(request));
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
        return new FlightRequest(
                "W43381",
                "CLJ",
                "FCO",
                Instant.parse("2026-10-10T08:00:00Z"),
                Instant.parse("2026-10-10T10:00:00Z"),
                180
        );
    }
}