package integra.vacation_planner_backend.dto;

public record LoginResponse(
    String token,
    String tokenType
) {
}
