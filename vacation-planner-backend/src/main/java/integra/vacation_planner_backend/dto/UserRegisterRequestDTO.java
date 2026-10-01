package integra.vacation_planner_backend.dto;

public record UserRegisterRequestDTO(
        String username,
        String email,
        String firstName,
        String lastName
) {}