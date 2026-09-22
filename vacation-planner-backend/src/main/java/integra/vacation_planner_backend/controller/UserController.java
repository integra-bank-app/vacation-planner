package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.UserRegisterRequestDTO;
import integra.vacation_planner_backend.dto.UserResponseDTO;
import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UUID> register(@RequestBody UserRegisterRequestDTO request) {
        try {
            UUID userId = userService.register(
                    request.getUsername(),
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName()
            );
            return ResponseEntity.ok(userId);

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("exists")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
    }

    @GetMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestParam String username) {
        try {
            User user = userService.login(username);
            UserResponseDTO response = new UserResponseDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}