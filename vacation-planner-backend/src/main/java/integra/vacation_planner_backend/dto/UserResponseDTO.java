package integra.vacation_planner_backend.dto;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName
) {}