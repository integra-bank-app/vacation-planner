package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.dto.LoginRequest;
import integra.vacation_planner_backend.dto.LoginResponse;
import integra.vacation_planner_backend.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public AuthController(
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (request.username() == null || request.username().isBlank()
            || request.password() == null || request.password().isBlank()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        var authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                request.username(),
                request.password()
            )
        );

        String role = authentication.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring(5))
            .findFirst()
            .orElseThrow(() ->
                new BadCredentialsException("Invalid credentials")
            );

        String token = jwtService.generateToken(
            authentication.getName(),
            role
        );

        return new LoginResponse(token, "Bearer");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationFailure(
        AuthenticationException exception
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Invalid username or password"));
    }
}
