package org.example.planner_backend.controller;

import org.example.planner_backend.dto.user.UserResponseDto;
import org.example.planner_backend.dto.user.UserUpdateRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.service.JwtService;
import org.example.planner_backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@WithMockUser(username = "user@example.com", roles = "USER")
class UserControllerTest {

    private static final UUID USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String EMAIL = "user@example.com";
    private static final String DISPLAY_NAME = "Test User";
    private static final String AVATAR_URL = "https://example.com/avatar.jpg";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    // ---------- GET /api/v1/users/me ----------

    @Test
    void test_shouldReturn200WithUserInfo() throws Exception {
        UserResponseDto response = new UserResponseDto(USER_ID, EMAIL, DISPLAY_NAME, AVATAR_URL, Role.USER);
        when(userService.getUser(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.displayName").value(DISPLAY_NAME))
                .andExpect(jsonPath("$.avatarUrl").value(AVATAR_URL))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void test_shouldReturn404WhenGetMeUserNotFound() throws Exception {
        when(userService.getUser(any()))
                .thenThrow(new ResourceNotFoundException("User does not exist"));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /api/v1/users/me ----------

    @Test
    void test_shouldReturn200WhenUpdateProfileSucceeds() throws Exception {
        UserResponseDto response = new UserResponseDto(USER_ID, EMAIL, "New Name", AVATAR_URL, Role.USER);
        when(userService.update(any(), any(UserUpdateRequestDto.class))).thenReturn(response);

        String body = """
                {"displayName": "New Name", "avatarUrl": "https://example.com/avatar.jpg"}
                """;

        mockMvc.perform(put("/api/v1/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("New Name"));
    }

    @Test
    void test_shouldReturn400WhenUpdateProfileNameBlank() throws Exception {
        String body = """
                {"displayName": "", "avatarUrl": null}
                """;

        mockMvc.perform(put("/api/v1/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn400WhenUpdateProfileNameTooLong() throws Exception {
        String tooLong = "a".repeat(101);
        String body = String.format("{\"displayName\": \"%s\", \"avatarUrl\": null}", tooLong);

        mockMvc.perform(put("/api/v1/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT /api/v1/users/me/password ----------

    @Test
    void test_shouldReturn204WhenChangePasswordSucceeds() throws Exception {
        String body = """
                {"currentPassword": "oldpass123", "newPassword": "newpass456"}
                """;

        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_shouldReturn400WhenChangePasswordCurrentBlank() throws Exception {
        String body = """
                {"currentPassword": "", "newPassword": "newpass456"}
                """;

        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn400WhenChangePasswordNewTooShort() throws Exception {
        String body = """
                {"currentPassword": "oldpass123", "newPassword": "short"}
                """;

        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn401WhenChangePasswordCurrentWrong() throws Exception {
        doThrow(new UnauthorizedException("Current password is incorrect"))
                .when(userService).changePassword(any(), any());

        String body = """
                {"currentPassword": "wrong", "newPassword": "newpass456"}
                """;

        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_shouldReturn409WhenChangePasswordNewEqualsCurrent() throws Exception {
        doThrow(new ConflictException("New password must differ from current password"))
                .when(userService).changePassword(any(), any());

        String body = """
                {"currentPassword": "samepass123", "newPassword": "samepass123"}
                """;

        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
