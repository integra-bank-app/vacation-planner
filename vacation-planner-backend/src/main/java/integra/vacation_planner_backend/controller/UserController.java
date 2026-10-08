package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.dto.UserRegisterRequestDTO;
import integra.vacation_planner_backend.dto.UserResponseDTO;
import integra.vacation_planner_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UUID> register(@RequestBody UserRegisterRequestDTO request) {
        UUID userId = userService.register(
                request.username(),
                request.email(),
                request.firstName(),
                request.lastName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    @GetMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestParam String username) {
        User user = userService.login(username);
        UserResponseDTO response = new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
        return ResponseEntity.ok(response);
    }
}
