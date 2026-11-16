package integra.vacation_planner_backend.controller;

import integra.vacation_planner_backend.exception.InvalidUserDataException;
import integra.vacation_planner_backend.exception.UserAlreadyExistsException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String USERNAME = "anamaria";

    private static final String EMAIL = "anamaria@example.com";

    private static final String PASSWORD = "parola123";

    private static final String FIRST_NAME = "Ana";

    private static final String LAST_NAME = "Popescu";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void register_validRequest_returns201AndId() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.register(USERNAME, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME)).thenReturn(userId);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + userId + "\""));
    }

    @Test
    void register_existingUser_returns409() throws Exception {
        when(userService.register(any(), any(), any(), any(), any()))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isConflict());
    }

    @Test
    void register_invalidData_returns400() throws Exception {
        when(userService.register(any(), any(), any(), any(), any()))
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

    private String validRequestJson() {
        return """
                {
                  "username": "anamaria",
                  "email": "anamaria@example.com",
                  "password": "parola123",
                  "firstName": "Ana",
                  "lastName": "Popescu"
                }
                """;
    }
}
