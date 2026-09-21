package integra.vacation_planner_backend.controller;

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
    public ResponseEntity<UUID> register(@RequestBody User user){
        try {
            UUID registeredUserId = userService.register(user);
            return ResponseEntity.ok(registeredUserId);
        } catch (IllegalArgumentException validationException){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validationException.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestParam String username){
        try {
            User authenticatedUser = userService.login(username);
            return ResponseEntity.ok(authenticatedUser);
        }catch (IllegalArgumentException notFoundException){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
