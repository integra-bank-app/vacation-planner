package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service


public class UserService {
    private final UserRepository userRepository;
    public UUID register(User user) {
        if (!user.getUsername().matches("^[a-z0-9]{3,20}$")) {
            throw new IllegalArgumentException("Username should contain only lowercase letters and numbers, and be between 3 and 20 characters long");
        }

        if(userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (!user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Format email invalid");
        }

        if(userRepository.existsByEmail(user.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }
    public User login(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Invalid username"));
    }

}
