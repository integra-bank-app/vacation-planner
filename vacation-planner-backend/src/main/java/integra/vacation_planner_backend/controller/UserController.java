package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.UserRegisterRequestDTO;
import integra.vacation_planner_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
}
