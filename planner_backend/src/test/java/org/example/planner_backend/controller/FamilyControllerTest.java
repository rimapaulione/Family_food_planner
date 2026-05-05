package org.example.planner_backend.controller;

import org.example.planner_backend.dto.family.FamilyMemberDto;
import org.example.planner_backend.dto.family.FamilyRequestDto;
import org.example.planner_backend.dto.family.FamilyResponseDto;
import org.example.planner_backend.dto.family.FamilySettingsRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.model.entity.MealServings;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.service.FamilyService;
import org.example.planner_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FamilyController.class)
@WithMockUser(username = "user@example.com", roles = "USER")
class FamilyControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FAMILY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String EMAIL = "user@example.com";
    private static final String FAMILY_NAME = "Smith Family";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FamilyService familyService;

    @MockitoBean
    private JwtService jwtService;

    private FamilyResponseDto sampleResponse() {
        return new FamilyResponseDto(
                FAMILY_ID,
                FAMILY_NAME,
                DayOfWeek.SUNDAY,
                new MealServings(3, null, 4),
                new MealServings(4, 4, 4),
                14,
                false,
                List.of(new FamilyMemberDto(USER_ID, "Test User", EMAIL, null, Role.ADMIN))
        );
    }

    // ---------- POST /api/v1/families ----------

    @Test
    void test_shouldReturn201WhenCreateFamilySucceeds() throws Exception {
        when(familyService.create(any(), any(FamilyRequestDto.class))).thenReturn(sampleResponse());

        String body = """
                {"name": "Smith Family"}
                """;

        mockMvc.perform(post("/api/v1/families")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(FAMILY_ID.toString()))
                .andExpect(jsonPath("$.name").value(FAMILY_NAME))
                .andExpect(jsonPath("$.isSetupCompleted").value(false));
    }

    @Test
    void test_shouldReturn400WhenCreateFamilyNameBlank() throws Exception {
        String body = """
                {"name": ""}
                """;

        mockMvc.perform(post("/api/v1/families")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn409WhenUserAlreadyHasFamily() throws Exception {
        when(familyService.create(any(), any(FamilyRequestDto.class)))
                .thenThrow(new ConflictException("User already has a family"));

        String body = """
                {"name": "Smith Family"}
                """;

        mockMvc.perform(post("/api/v1/families")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ---------- GET /api/v1/families/me ----------

    @Test
    void test_shouldReturn200WithFamily() throws Exception {
        when(familyService.getMyFamily(any())).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/families/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FAMILY_ID.toString()))
                .andExpect(jsonPath("$.members[0].role").value("ADMIN"));
    }

    @Test
    void test_shouldReturn404WhenUserHasNoFamily() throws Exception {
        when(familyService.getMyFamily(any()))
                .thenThrow(new ResourceNotFoundException("User has no family"));

        mockMvc.perform(get("/api/v1/families/me"))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /api/v1/families/me ----------

    @Test
    void test_shouldReturn200WhenUpdateSettingsSucceeds() throws Exception {
        when(familyService.updateSettings(any(), any(FamilySettingsRequestDto.class)))
                .thenReturn(sampleResponse());

        String body = """
                {
                  "name": "New Name",
                  "shoppingDay": "MONDAY",
                  "defaultWeekdayServings": {"breakfast": 3, "lunch": null, "dinner": 4},
                  "defaultWeekendServings": {"breakfast": 4, "lunch": 4, "dinner": 4},
                  "noRepeatRecipeDays": 14,
                  "isSetupCompleted": true
                }
                """;

        mockMvc.perform(patch("/api/v1/families/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void test_shouldReturn400WhenUpdateSettingsNameBlank() throws Exception {
        String body = """
                {
                  "name": "",
                  "shoppingDay": "MONDAY",
                  "defaultWeekdayServings": {"breakfast": 3, "lunch": null, "dinner": 4},
                  "defaultWeekendServings": {"breakfast": 4, "lunch": 4, "dinner": 4},
                  "isSetupCompleted": true
                }
                """;

        mockMvc.perform(patch("/api/v1/families/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn400WhenUpdateSettingsShoppingDayMissing() throws Exception {
        String body = """
                {
                  "name": "Family",
                  "defaultWeekdayServings": {"breakfast": 3, "lunch": null, "dinner": 4},
                  "defaultWeekendServings": {"breakfast": 4, "lunch": 4, "dinner": 4},
                  "isSetupCompleted": true
                }
                """;

        mockMvc.perform(patch("/api/v1/families/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn400WhenUpdateSettingsServingOutOfRange() throws Exception {
        String body = """
                {
                  "name": "Family",
                  "shoppingDay": "MONDAY",
                  "defaultWeekdayServings": {"breakfast": 99, "lunch": null, "dinner": 4},
                  "defaultWeekendServings": {"breakfast": 4, "lunch": 4, "dinner": 4},
                  "isSetupCompleted": true
                }
                """;

        mockMvc.perform(patch("/api/v1/families/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
