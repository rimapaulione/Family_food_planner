package org.example.planner_backend.controller;

import org.example.planner_backend.dto.mealplan.MealPlanResponseDto;
import org.example.planner_backend.dto.mealplan.MealPlanUpdateRequestDto;
import org.example.planner_backend.dto.mealplan.MealPlanWindowResponseDto;
import org.example.planner_backend.dto.mealplan.MealSlotResponseDto;
import org.example.planner_backend.dto.mealplan.MealSlotUpdateRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.model.enums.MealType;
import org.example.planner_backend.model.enums.PlanStatus;
import org.example.planner_backend.service.JwtService;
import org.example.planner_backend.service.MealPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealPlanController.class)
@WithMockUser(username = "user@example.com", roles = "USER")
class MealPlanControllerTest {

    private static final UUID PLAN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SLOT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RECIPE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDate WEEK_START = LocalDate.of(2026, 5, 4);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 5, 10);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MealPlanService mealPlanService;

    @MockitoBean
    private JwtService jwtService;

    private MealSlotResponseDto sampleSlot() {
        return new MealSlotResponseDto(SLOT_ID, WEEK_START, MealType.BREAKFAST, null, null, null, null);
    }

    private MealPlanResponseDto samplePlan() {
        return new MealPlanResponseDto(PLAN_ID, WEEK_START, WEEK_END, PlanStatus.DRAFT, List.of(sampleSlot()));
    }

    private MealPlanWindowResponseDto sampleWindow() {
        return new MealPlanWindowResponseDto(samplePlan(), samplePlan());
    }

    // ---------- GET /api/v1/meal-plans/current-and-next ----------

    @Test
    void test_shouldReturn200WithWindow() throws Exception {
        when(mealPlanService.getCurrentAndNext(any())).thenReturn(sampleWindow());

        mockMvc.perform(get("/api/v1/meal-plans/current-and-next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentWeek.id").value(PLAN_ID.toString()))
                .andExpect(jsonPath("$.currentWeek.status").value("DRAFT"))
                .andExpect(jsonPath("$.nextWeek.id").value(PLAN_ID.toString()));
    }

    @Test
    void test_shouldReturn404WhenUserHasNoFamily() throws Exception {
        when(mealPlanService.getCurrentAndNext(any()))
                .thenThrow(new ResourceNotFoundException("User has no family"));

        mockMvc.perform(get("/api/v1/meal-plans/current-and-next"))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /api/v1/meal-plans/{id} ----------

    @Test
    void test_shouldReturn200WhenLockPlanSucceeds() throws Exception {
        MealPlanResponseDto locked = new MealPlanResponseDto(
                PLAN_ID, WEEK_START, WEEK_END, PlanStatus.LOCKED, List.of(sampleSlot()));
        when(mealPlanService.updatePlan(any(), eq(PLAN_ID), any(MealPlanUpdateRequestDto.class)))
                .thenReturn(locked);

        String body = """
                {"status": "LOCKED"}
                """;

        mockMvc.perform(patch("/api/v1/meal-plans/{id}", PLAN_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"));
    }

    @Test
    void test_shouldReturn404WhenPlanNotFound() throws Exception {
        when(mealPlanService.updatePlan(any(), any(), any(MealPlanUpdateRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Meal plan not found"));

        String body = """
                {"status": "LOCKED"}
                """;

        mockMvc.perform(patch("/api/v1/meal-plans/{id}", PLAN_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_shouldReturn401WhenUpdatePlanNotAdmin() throws Exception {
        when(mealPlanService.updatePlan(any(), any(), any(MealPlanUpdateRequestDto.class)))
                .thenThrow(new UnauthorizedException("Admin role required"));

        String body = """
                {"status": "LOCKED"}
                """;

        mockMvc.perform(patch("/api/v1/meal-plans/{id}", PLAN_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ---------- PATCH /api/v1/meal-plans/slots/{id} ----------

    @Test
    void test_shouldReturn200WhenAssignRecipeToSlot() throws Exception {
        MealSlotResponseDto assigned = new MealSlotResponseDto(
                SLOT_ID, WEEK_START, MealType.BREAKFAST, RECIPE_ID, "Pasta", 30, 4);
        when(mealPlanService.updateSlot(any(), eq(SLOT_ID), any(MealSlotUpdateRequestDto.class)))
                .thenReturn(assigned);

        String body = """
                {"recipeId": "%s", "servings": 4}
                """.formatted(RECIPE_ID);

        mockMvc.perform(patch("/api/v1/meal-plans/slots/{id}", SLOT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(RECIPE_ID.toString()))
                .andExpect(jsonPath("$.recipeName").value("Pasta"))
                .andExpect(jsonPath("$.servings").value(4));
    }

    @Test
    void test_shouldReturn200WhenClearRecipeSlot() throws Exception {
        when(mealPlanService.updateSlot(any(), any(), any(MealSlotUpdateRequestDto.class)))
                .thenReturn(sampleSlot());

        String body = """
                {"recipeId": null, "servings": null}
                """;

        mockMvc.perform(patch("/api/v1/meal-plans/slots/{id}", SLOT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }


    @Test
    void test_shouldReturn400WhenSlotServingsZero() throws Exception {
        String body = """
                {"recipeId": "%s", "servings": 0}
                """.formatted(RECIPE_ID);

        mockMvc.perform(patch("/api/v1/meal-plans/slots/{id}", SLOT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn404WhenSlotNotFound() throws Exception {
        when(mealPlanService.updateSlot(any(), any(), any(MealSlotUpdateRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Meal slot not found"));

        String body = """
                {"recipeId": "%s", "servings": 4}
                """.formatted(RECIPE_ID);

        mockMvc.perform(patch("/api/v1/meal-plans/slots/{id}", SLOT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_shouldReturn401WhenSlotInDifferentFamily() throws Exception {
        when(mealPlanService.updateSlot(any(), any(), any(MealSlotUpdateRequestDto.class)))
                .thenThrow(new UnauthorizedException("Slot not in your family"));

        String body = """
                {"recipeId": "%s", "servings": 4}
                """.formatted(RECIPE_ID);

        mockMvc.perform(patch("/api/v1/meal-plans/slots/{id}", SLOT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void test_shouldReturn409WhenPlanLocked() throws Exception {
        when(mealPlanService.updateSlot(any(), any(), any(MealSlotUpdateRequestDto.class)))
                .thenThrow(new ConflictException("Cannot edit a locked plan"));

        String body = """
                {"recipeId": "%s", "servings": 4}
                """.formatted(RECIPE_ID);

        mockMvc.perform(patch("/api/v1/meal-plans/slots/{id}", SLOT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
