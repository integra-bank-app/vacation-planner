package integra.vacation_planner_backend.service;

import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.exception.InvalidUserDataException;
import integra.vacation_planner_backend.exception.UserAlreadyExistsException;
import integra.vacation_planner_backend.exception.UserNotFoundException;
import integra.vacation_planner_backend.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String USERNAME = "dariamaria";
    private static final String EMAIL = "dariamaria@example.com";
    private static final String FIRST_NAME = "Daria";
    private static final String LAST_NAME = "Berciu";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private void stubSaveReturningId(UUID id) {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId(id);
            return toSave;
        });
    }

    @Test
    void register_shouldSaveUserAndReturnId_whenDataIsValid() {
        UUID expectedId = UUID.randomUUID();
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        stubSaveReturningId(expectedId);

        UUID result = userService.register(USERNAME, EMAIL, FIRST_NAME, LAST_NAME);

        assertThat(result).isEqualTo(expectedId);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("dariamaria");
        assertThat(saved.getEmail()).isEqualTo("dariamaria@example.com");
        assertThat(saved.getFirstName()).isEqualTo("Daria");
        assertThat(saved.getLastName()).isEqualTo("Berciu");
    }

    @Nested
    class UsernameValidation {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"da", "dariamariaberciuberciu", "DariaMaria", "daria_maria", "daria maria", ""})
        void register_shouldThrowInvalidUserData_whenUsernameIsWrong(String username) {
            assertThatThrownBy(() -> userService.register(username, EMAIL, FIRST_NAME, LAST_NAME))
                    .isInstanceOf(InvalidUserDataException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        void register_shouldThrowUserAlreadyExists_whenUsernameIsTaken() {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

            assertThatThrownBy(() -> userService.register(USERNAME, EMAIL, FIRST_NAME, LAST_NAME))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("Username");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class EmailValidation {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"not-an-email", "daria@", "@example.com", "daria@example", "daria..maria@example.com", ".daria@example.com", "daria@-example.com"})
        void register_shouldThrowInvalidUserData_whenEmailIsWrong(String email) {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);

            assertThatThrownBy(() -> userService.register(USERNAME, email, FIRST_NAME, LAST_NAME))
                    .isInstanceOf(InvalidUserDataException.class);

            verify(userRepository, never()).save(any());
        }

        @ParameterizedTest
        @ValueSource(strings = {"daria@example.com", "daria.maria@example.com", "daria+filtru@example.com", "daria@sub.example.co.uk", "daria@example.agency", "daria.maria@stud.ubbcluj.ro"})
        void register_shouldAcceptValidEmailFormats(String email) {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(email)).thenReturn(false);
            stubSaveReturningId(UUID.randomUUID());

            assertThat(userService.register(USERNAME, email, FIRST_NAME, LAST_NAME)).isNotNull();
        }

        @Test
        void register_shouldThrowUserAlreadyExists_whenEmailIsTaken() {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> userService.register(USERNAME, EMAIL, FIRST_NAME, LAST_NAME))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("Email");
        }
    }

    @Nested
    class NameValidation {

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        void register_shouldThrowInvalidUserData_whenFirstNameIsMissing(String firstName) {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

            assertThatThrownBy(() -> userService.register(USERNAME, EMAIL, firstName, LAST_NAME))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessageContaining("First name");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        void register_shouldThrowInvalidUserData_whenLastNameIsMissing(String lastName) {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

            assertThatThrownBy(() -> userService.register(USERNAME, EMAIL, FIRST_NAME, lastName))
                    .isInstanceOf(InvalidUserDataException.class)
                    .hasMessageContaining("Last name");
        }

        @Test
        void register_shouldThrowInvalidUserData_whenNameIsTooLong() {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

            String tooLong = "a".repeat(51);

            assertThatThrownBy(() -> userService.register(USERNAME, EMAIL, tooLong, LAST_NAME))
                    .isInstanceOf(InvalidUserDataException.class);
        }

        @Test
        void register_shouldAcceptNamesWithDiacriticsAndHyphen() {
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            stubSaveReturningId(UUID.randomUUID());

            assertThat(userService.register(USERNAME, EMAIL, "Daria-Maria", "Berciu")).isNotNull();
        }
    }

    @Nested
    class Login {

        @Test
        void login_shouldReturnUser_whenUsernameExists() {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setUsername(USERNAME);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

            assertThat(userService.login(USERNAME)).isSameAs(user);
        }

        @Test
        void login_shouldThrowUserNotFound_whenUsernameDoesNotExist() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login("unknown"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}