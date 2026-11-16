package integra.vacation_planner_backend.dto;

public record UserRegisterRequestDTO(
        String username,
        String email,
        String password,
        String firstName,
        String lastName
) { }
