package integra.vacation_planner_backend.exception;

public class UnauthorizedTripAccessException extends RuntimeException {
    public UnauthorizedTripAccessException(String message) {
        super(message);
    }
}
