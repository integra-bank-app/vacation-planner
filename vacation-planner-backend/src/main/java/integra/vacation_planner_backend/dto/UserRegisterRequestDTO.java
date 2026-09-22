package integra.vacation_planner_backend.dto;

import lombok.Data;

@Data
public class UserRegisterRequestDTO {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}