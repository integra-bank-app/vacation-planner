package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Flight;
import integra.vacation_planner_backend.domain.Accommodation;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.dto.FlightResponse;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.TripAccommodationRequest;
import integra.vacation_planner_backend.dto.TripAccommodationResponse;
import integra.vacation_planner_backend.dto.TripDTO;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.repository.FlightRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.dto.TripResponse;
import integra.vacation_planner_backend.exception.ForbiddenException;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.exception.UserNotFoundException;
import integra.vacation_planner_backend.repository.AccommodationRepository;
import integra.vacation_planner_backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TripService {

    private final TripRepository tripRepository;

    private final AccommodationRepository accommodationRepository;

    private final UserRepository userRepository;

    private final FlightRepository flightRepository;

    public TripService(
        TripRepository tripRepository,
        AccommodationRepository accommodationRepository,
        UserRepository userRepository,
        FlightRepository flightRepository
    ) {
        this.tripRepository = tripRepository;
        this.accommodationRepository = accommodationRepository;
        this.userRepository = userRepository;
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

        User user = findUser(dto.getUsername());

        Trip trip = new Trip();
        trip.setDestination(dto.getDestination());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setUser(user);

        return tripRepository.save(trip).getId();
    }

    public List<TripResponse> getTripsBetweenDates(
        String username,
        LocalDate startDate,
        LocalDate endDate
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        User user = findUser(username);

        return tripRepository
            .findByUserAndStartDateGreaterThanEqualAndEndDateLessThanEqual(
                user, startDate, endDate
            )
            .stream()
            .map(trip -> new TripResponse(
                trip.getId(),
                user.getUsername(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FlightResponse> getTripFlights(UUID tripId, String username) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (!trip.getUser().getUsername().equals(username)) {
            throw new UnauthorizedTripAccessException(
                "User is not authorized to view the flights for this trip"
            );
        }

        return trip.getFlights().stream()
            .sorted(Comparator.comparing(Flight::getDepartureTime))
            .map(flight -> new FlightResponse(
                flight.getFlightNumber(),
                flight.getDepartureAirportCode(),
                flight.getArrivalAirportCode(),
                flight.getDepartureTime().atZone(ZoneId.systemDefault()),
                flight.getArrivalTime().atZone(ZoneId.systemDefault()),
                flight.getPrice()
            ))
            .toList();
    }

    @Transactional
    public void updateTrip(UUID tripId, TripDTO dto) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        if (!trip.getUser().getUsername().equals(dto.getUsername())) {
            throw new UnauthorizedTripAccessException("Not allowed to update this trip");
        }

        if (dto.getStartDate() == null && dto.getEndDate() == null) {
            throw new IllegalArgumentException("At least one date must be provided");
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
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        trip.setStartDate(newStartDate);
        trip.setEndDate(newEndDate);
        tripRepository.save(trip);
    }

    @Transactional
    public void deleteTrip(UUID tripId, String username) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        if (!trip.getUser().getUsername().equals(username)) {
            throw new UnauthorizedTripAccessException("Not allowed to delete this trip");
        }

        tripRepository.delete(trip);
    }

    @Transactional
    public void addFlightToTrip(UUID tripId, String username, String flightNumber) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() ->  new TripNotFoundException("Trip not found"));

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        if (!trip.getUser().getUsername().equals(username)) {
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

    @Transactional
    public void deleteFlightFromTrip(
        UUID tripId,
        String flightNumber,
        String username
    ) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new TripNotFoundException("Trip not found"));

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (!trip.getUser().getUsername().equals(username)) {
            throw new UnauthorizedTripAccessException(
                "User is not authorized to remove flights from this trip"
            );
        }

        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("Flight number is required");
        }

        Flight flight = flightRepository.findByFlightNumber(flightNumber)
            .orElseThrow(() -> new NotFoundException("Flight not found"));

        boolean removed = trip.getFlights().removeIf(
            existingFlight -> existingFlight.getId().equals(flight.getId())
        );

        if (!removed) {
            throw new NotFoundException("Flight is not part of this trip");
        }

        tripRepository.save(trip);
    }

    @Transactional
    public void addAccommodationToTrip(UUID tripId, TripAccommodationRequest request) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new NotFoundException("Trip not found"));

        Accommodation accommodation = accommodationRepository
            .findById(request.accommodationId())
            .orElseThrow(() -> new NotFoundException("Accommodation not found"));

        if (!trip.getUser().getUsername().equals(request.username())) {
            throw new ForbiddenException("User is not the owner of this trip");
        }

        if (trip.getAccommodations().contains(accommodation)) {
            throw new ConflictException("Accommodation already added to trip");
        }

        trip.getAccommodations().add(accommodation);
        tripRepository.save(trip);
    }

    @Transactional
    public List<TripAccommodationResponse> getTripAccommodations(
        UUID tripId,
        String username
    ) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new NotFoundException("Trip not found"));

        if (!trip.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("User is not the owner of this trip");
        }

        return trip.getAccommodations()
            .stream()
            .sorted(Comparator.comparing(Accommodation::getPricePerNight))
            .map(accommodation -> new TripAccommodationResponse(
                accommodation.getId(),
                accommodation.getName(),
                accommodation.getAddress(),
                accommodation.getCity(),
                accommodation.getPricePerNight()
            ))
            .toList();
    }

    @Transactional
    public void removeAccommodationFromTrip(
        UUID tripId,
        UUID accommodationId,
        String username
    ) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new NotFoundException("Trip not found"));

        if (!trip.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("User is not the owner of this trip");
        }

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
            .orElseThrow(() -> new NotFoundException("Accommodation not found"));

        if (!trip.getAccommodations().contains(accommodation)) {
            throw new NotFoundException("Accommodation not found in trip");
        }

        trip.getAccommodations().remove(accommodation);
        tripRepository.save(trip);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
