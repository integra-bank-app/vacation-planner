package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.exception.InvalidUserDataException;
import integra.vacation_planner_backend.exception.UserAlreadyExistsException;
import integra.vacation_planner_backend.exception.UserNotFoundException;
import integra.vacation_planner_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private static final String USERNAME_REGEX = "^[a-z0-9]{3,20}$";

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9_+-]+(\\.[A-Za-z0-9_+-]+)*@([A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$";

    private static final int MAX_NAME_LENGTH = 50;

    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final int MAX_PASSWORD_LENGTH = 72;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public UUID register(String username, String email, String password, String firstName, String lastName) {
        if (username == null || !username.matches(USERNAME_REGEX)) {
            throw new InvalidUserDataException(
                    "Username should contain only lowercase letters and numbers, and be between 3 and 20 characters long");
        }
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (email == null || !email.matches(EMAIL_REGEX)) {
            throw new InvalidUserDataException("Invalid email format");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists");
        }
        validatePassword(password);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);

        return userRepository.save(user).getId();
    }

    public User login(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH
            || password.length() > MAX_PASSWORD_LENGTH) {
            throw new InvalidUserDataException(
                "Password must be between " + MIN_PASSWORD_LENGTH + " and "
                    + MAX_PASSWORD_LENGTH + " characters long");
        }
    }

    private void validateName(String name, String fieldLabel) {
        if (name == null || name.isBlank()) {
            throw new InvalidUserDataException(fieldLabel + " is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidUserDataException(
                    fieldLabel + " must be at most " + MAX_NAME_LENGTH + " characters long");
        }
    }
}
