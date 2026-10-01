package integra.vacation_planner_backend.exception;

public class ValidatorException extends RuntimeException{
    public ValidatorException(String message) {
        super(message);
    }
}
