package org.example.planner_backend.controller;

import org.example.planner_backend.dto.auth.AuthResponseDto;
import org.example.planner_backend.dto.auth.LoginRequestDto;
import org.example.planner_backend.dto.auth.RegisterRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.service.AuthService;
import org.example.planner_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@WithMockUser
class AuthControllerTest {

    private static final UUID USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String EMAIL = "user@example.com";
    private static final String DISPLAY_NAME = "Test User";
    private static final String TOKEN = "fake.jwt.token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    // ---------- POST /api/v1/auth/register ----------

    @Test
    void test_shouldReturn201WithTokenWhenRegisterSucceeds() throws Exception {
        AuthResponseDto response = new AuthResponseDto(TOKEN, USER_ID, EMAIL, DISPLAY_NAME, Role.USER);
        when(authService.register(any(RegisterRequestDto.class))).thenReturn(response);

        String body = """
                {"email": "user@example.com", "password": "password123", "displayName": "Test User"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(TOKEN))
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.displayName").value(DISPLAY_NAME))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void test_shouldReturn409WhenRegisterEmailExists() throws Exception {
        when(authService.register(any(RegisterRequestDto.class)))
                .thenThrow(new ConflictException("Email already exists"));

        String body = """
                {"email": "user@example.com", "password": "password123", "displayName": "Test User"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void test_shouldReturn400WhenRegisterEmailBlank() throws Exception {
        String body = """
                {"email": "", "password": "password123", "displayName": "Test User"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /api/v1/auth/login ----------

    @Test
    void test_shouldReturn200WithTokenWhenLoginSucceeds() throws Exception {
        AuthResponseDto response = new AuthResponseDto(TOKEN, USER_ID, EMAIL, DISPLAY_NAME, Role.USER);
        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        String body = """
                {"email": "user@example.com", "password": "password123"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN))
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void test_shouldReturn401WhenLoginWrongPassword() throws Exception {
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        String body = """
                {"email": "user@example.com", "password": "wrongpass"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_shouldReturn401WhenLoginUnknownEmail() throws Exception {
        when(authService.login(any(LoginRequestDto.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        String body = """
                {"email": "unknown@example.com", "password": "password123"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
