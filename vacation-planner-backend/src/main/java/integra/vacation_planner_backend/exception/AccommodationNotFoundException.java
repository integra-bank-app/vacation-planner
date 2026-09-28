package integra.vacation_planner_backend.exception;

import java.util.UUID;

public class AccommodationNotFoundException extends RuntimeException {

    public AccommodationNotFoundException(UUID id) {
        super("Accommodation with id " + id + " was not found");
    }
}