package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.domain.User;
import integra.vacation_planner_backend.exception.InvalidUserDataException;
import integra.vacation_planner_backend.exception.UserAlreadyExistsException;
import integra.vacation_planner_backend.exception.UserNotFoundException;
import integra.vacation_planner_backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String USERNAME = "dariamaria";

    private static final String EMAIL = "daria@example.com";

    private static final String FIRST_NAME = "Daria";

    private static final String LAST_NAME = "Maria";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void register_validRequest_returns201AndId() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.register(USERNAME, EMAIL, FIRST_NAME, LAST_NAME)).thenReturn(userId);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + userId + "\""));
    }

    @Test
    void register_existingUser_returns409() throws Exception {
        when(userService.register(any(), any(), any(), any()))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isConflict());
    }

    @Test
    void register_invalidData_returns400() throws Exception {
        when(userService.register(any(), any(), any(), any()))
                .thenThrow(new InvalidUserDataException("Invalid email format"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json }"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }


    @Test
    void login_existingUser_returns200AndUser() throws Exception {
        when(userService.login(USERNAME)).thenReturn(existingUser());

        mockMvc.perform(get("/login").param("username", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("dariamaria"))
                .andExpect(jsonPath("$.email").value("daria@example.com"))
                .andExpect(jsonPath("$.firstName").value("Daria"))
                .andExpect(jsonPath("$.lastName").value("Maria"));
    }

    @Test
    void login_missingUser_returns404() throws Exception {
        when(userService.login("unknown")).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/login").param("username", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_missingUsernameParam_returns400() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    private String validRequestJson() {
        return """
                {
                  "username": "dariamaria",
                  "email": "daria@example.com",
                  "firstName": "Daria",
                  "lastName": "Maria"
                }
                """;
    }

    private User existingUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(USERNAME);
        user.setEmail(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        return user;
    }
}
