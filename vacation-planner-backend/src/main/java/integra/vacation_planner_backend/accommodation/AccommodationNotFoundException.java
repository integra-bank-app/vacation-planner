package integra.vacation_planner_backend.accommodation;

public class AccommodationNotFoundException extends RuntimeException {

    public AccommodationNotFoundException(Long id) {
        super("Accommodation with id " + id + " was not found");
    }
}