package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.Restaurant;
import integra.vacation_planner_backend.domain.Trip;
import integra.vacation_planner_backend.domain.TripRestaurant;
import integra.vacation_planner_backend.dto.TripRestaurantResponseDTO;
import integra.vacation_planner_backend.exception.ConflictException;
import integra.vacation_planner_backend.exception.NotFoundException;
import integra.vacation_planner_backend.exception.TripNotFoundException;
import integra.vacation_planner_backend.exception.UnauthorizedTripAccessException;
import integra.vacation_planner_backend.repository.RestaurantRepository;
import integra.vacation_planner_backend.repository.TripRepository;
import integra.vacation_planner_backend.repository.TripRestaurantRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class TripRestaurantService {

    private final TripRestaurantRepository tripRestaurantRepository;

    private final TripRepository tripRepository;

    private final RestaurantRepository restaurantRepository;

    public TripRestaurantService(
        TripRestaurantRepository tripRestaurantRepository,
        TripRepository tripRepository,
        RestaurantRepository restaurantRepository
    ) {
        this.tripRestaurantRepository = tripRestaurantRepository;
        this.tripRepository = tripRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public UUID addRestaurantToTrip(
        UUID tripId,
        String username,
        UUID restaurantId,
        LocalDate visitDate,
        LocalTime visitTime
    ) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() ->
            new TripNotFoundException("Trip not found: " + tripId));

        if (!trip.getUser().getUsername().equals(username)) {
            throw new UnauthorizedTripAccessException(
                "You do not have permission to modify this trip"
            );
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
            .orElseThrow(() ->
                new NotFoundException("Restaurant not found: " + restaurantId));

        if (visitDate == null
                || visitDate.isBefore(trip.getStartDate())
                || visitDate.isAfter(trip.getEndDate())) {
            throw new IllegalArgumentException(
                "Visit date must be withing the trip dates"
            );
        }

        if (visitTime == null || !isWithinOpeningHours(
            visitTime,
            restaurant.getOpeningHour(),
            restaurant.getClosingHour()
        )) {
            throw new IllegalArgumentException(
                "Visit time is outside the restaurant's opening hours"
            );
        }

        boolean duplicate = tripRestaurantRepository
            .existsByTrip_IdAndRestaurant_IdAndVisitDateAndVisitTime(
                tripId, restaurantId, visitDate, visitTime
            );

        if (duplicate) {
            throw new ConflictException(
                "This restaurant is already scheduled for thi date and time"
            );
        }

        TripRestaurant visit = new TripRestaurant(
            trip, restaurant, visitDate, visitTime
        );

        return tripRestaurantRepository.save(visit).getId();
    }

    @Transactional(readOnly = true)
    public List<TripRestaurantResponseDTO> getRestaurantsForTrip(
        UUID tripId,
        String username
    ) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() ->
                new TripNotFoundException("Trip not found: " + tripId));

        if (!trip.getUser().getUsername().equals(username)) {
            throw new UnauthorizedTripAccessException(
                "You do not have permission to view this trip"
            );
        }

        return tripRestaurantRepository
            .findByTrip_IdOrderByVisitDateAscVisitTimeAsc(tripId)
            .stream()
            .map(visit -> new TripRestaurantResponseDTO(
                visit.getId(),
                visit.getRestaurant().getId(),
                visit.getRestaurant().getName(),
                visit.getRestaurant().getAddress(),
                visit.getVisitDate(),
                visit.getVisitTime()
            ))
            .toList();
    }

    @Transactional
    public void removeRestaurantFromTrip(
        UUID tripId,
        UUID visitId,
        String username
    ) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() ->
                new TripNotFoundException("Trip not found: " + tripId));

        if (!trip.getUser().getUsername().equals(username)) {
            throw new UnauthorizedTripAccessException(
                "You do not have permission to modify this trip"
            );
        }

        TripRestaurant visit = tripRestaurantRepository.findById(visitId)
            .orElseThrow(() ->
                new NotFoundException("Scheduled restaurant visit not found: " + visitId));

        if (!visit.getTrip().getId().equals(tripId)) {
            throw new NotFoundException(
                "Scheduled restaurant visit not found for this trip"
            );
        }

        tripRestaurantRepository.delete(visit);
    }

    private boolean isWithinOpeningHours(
        LocalTime visitTime,
        LocalTime openingHour,
        LocalTime closingHour
    ) {
        if (visitTime == null || openingHour == null || closingHour == null) {
            return false;
        }

        if (openingHour.isBefore(closingHour)) {
            return !visitTime.isBefore(openingHour)
                && visitTime.isBefore(closingHour);
        }

        // Overnight hours like 18:00-02:00.
        if (openingHour.isAfter(closingHour)) {
            return !visitTime.isBefore(openingHour)
                || visitTime.isBefore(closingHour);
        }

        return false;
    }
}
