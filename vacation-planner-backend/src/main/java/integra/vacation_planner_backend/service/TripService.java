package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.repository.FlightRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TripService {
    private final TripRepository tripRepository;

    private final FlightRepository flightRepository;

    public TripService(TripRepository tripRepository, FlightRepository flightRepository) {
        this.tripRepository = tripRepository;
        this.flightRepository = flightRepository;
    }

    public UUID createTrip(TripDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (dto.getDestination() == null || dto.getDestination().isBlank()) {
            throw new IllegalArgumentException("destination is required");
        }
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        Trip trip = new Trip();
        trip.setDestination(dto.getDestination());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setUsername(dto.getUsername());
        return tripRepository.save(trip).getId();
    }

    public List<Trip> getTripsBetweenDates(String username, LocalDate startDate, LocalDate endDate) {
        if (username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }
        return tripRepository.findByUsernameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(username, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Flight> getTripFlights(UUID tripId, String username) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (!trip.getUsername().equals(username)) {
            throw new UnauthorizedTripAccessException(
                "User is not authorized to view the flights for this trip"
            );
        }

        return trip.getFlights().stream()
            .sorted(Comparator.comparing(Flight::getDepartureTime))
            .toList();

    }

    public void updateTrip(UUID tripId, TripDTO dto) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        if (!trip.getUsername().equals(dto.getUsername())) {
            throw new UnauthorizedTripAccessException(
                    "Not allowed to update this trip"
            );
        }

        if (dto.getStartDate() == null && dto.getEndDate() == null) {
            throw new IllegalArgumentException(
                    "At least one date must be provided"
            );
        }

        LocalDate newStartDate = trip.getStartDate();
        LocalDate newEndDate = trip.getEndDate();

        if (dto.getStartDate() != null) {
            newStartDate = dto.getStartDate();
        }

        if (dto.getEndDate() != null) {
            newEndDate = dto.getEndDate();
        }

        if (newStartDate.isAfter(newEndDate)) {
            throw new IllegalArgumentException(
                    "startDate must be before endDate"
            );
        }

        if (dto.getStartDate() != null) {
            trip.setStartDate(dto.getStartDate());
        }

        if (dto.getEndDate() != null) {
            trip.setEndDate(dto.getEndDate());
        }

        tripRepository.save(trip);
    }

    @Transactional
    public void addFlightToTrip(UUID tripId, String username, String flightNumber) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() ->  new TripNotFoundException("Trip not found"));

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        if (!trip.getUsername().equals(username)) {
            throw new UnauthorizedTripAccessException(
                "Not allowed to add flights to this trip"
            );
        }

        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("flightNumber is required");
        }


        Flight flight = flightRepository.findByFlightNumberForUpdate(flightNumber)
            .orElseThrow(() -> new NotFoundException("Flight not found"));


        boolean alreadyAdded = trip.getFlights().stream()
            .anyMatch(existingFlight ->
                existingFlight.getId().equals(flight.getId()));

        if (alreadyAdded) {
            throw new ConflictException("Flight already added to this trip");
        }

        long bookedSeats = tripRepository.countTripsUsingFlight(flight.getId());

        if (bookedSeats >= flight.getNumberOfSeats()) {
            throw new ConflictException("Flight is full");
        }

        trip.getFlights().add(flight);
        tripRepository.save(trip);
    }

    public void deleteTrip(UUID tripId, String username) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->  new TripNotFoundException("Trip not found"));

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        if (!trip.getUsername().equals(username)) {
            throw new UnauthorizedTripAccessException(
                    "Not allowed to delete this trip"
            );
        }

        tripRepository.delete(trip);
    }
}
