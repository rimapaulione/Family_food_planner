package org.example.planner_backend.controller;

import org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientRequestDto;
import org.example.planner_backend.dto.ingredient.IngredientResponseDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.model.enums.Unit;
import org.example.planner_backend.service.IngredientService;
import org.example.planner_backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngredientController.class)
@WithMockUser(username = "user@example.com", roles = "USER")
class IngredientControllerTest {

    private static final UUID INGREDIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String INGREDIENT_NAME = "Pienas";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngredientService ingredientService;

    @MockitoBean
    private JwtService jwtService;

    // ---------- GET /api/v1/ingredients ----------

    @Test
    void test_shouldReturn200WithIngredientListWhenNoSearch() throws Exception {
        IngredientResponseDto response = new IngredientResponseDto(INGREDIENT_ID, INGREDIENT_NAME, Unit.ML);
        when(ingredientService.getAll(any(), eq(null))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(INGREDIENT_ID.toString()))
                .andExpect(jsonPath("$[0].nameLt").value(INGREDIENT_NAME))
                .andExpect(jsonPath("$[0].unit").value("ML"));
    }

    @Test
    void test_shouldReturn200WithIngredientListWhenSearchProvided() throws Exception {
        IngredientResponseDto response = new IngredientResponseDto(INGREDIENT_ID, INGREDIENT_NAME, Unit.ML);
        when(ingredientService.getAll(any(), eq("pie"))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/ingredients").param("search", "pie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nameLt").value(INGREDIENT_NAME));
    }

    @Test
    void test_shouldReturn400WhenSearchTooLong() throws Exception {
        String tooLong = "a".repeat(51);

        mockMvc.perform(get("/api/v1/ingredients").param("search", tooLong))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /api/v1/ingredients/with-recipes-count ----------

    @Test
    void test_shouldReturn200WithDetailList() throws Exception {
        IngredientDetailResponseDto detail = new IngredientDetailResponseDto(
                INGREDIENT_ID, INGREDIENT_NAME, Unit.ML, 2);
        when(ingredientService.getAllWithRecipesCount(any(), eq(null))).thenReturn(List.of(detail));

        mockMvc.perform(get("/api/v1/ingredients/with-recipes-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(INGREDIENT_ID.toString()))
                .andExpect(jsonPath("$[0].nameLt").value(INGREDIENT_NAME))
                .andExpect(jsonPath("$[0].unit").value("ML"))
                .andExpect(jsonPath("$[0].recipeCount").value(2));
    }

    // ---------- POST /api/v1/ingredients ----------

    @Test
    void test_shouldReturn201WhenCreateSucceeds() throws Exception {
        IngredientResponseDto response = new IngredientResponseDto(INGREDIENT_ID, INGREDIENT_NAME, Unit.ML);
        when(ingredientService.create(any(), any(IngredientRequestDto.class))).thenReturn(response);

        String body = """
                {"nameLt": "Pienas", "unit": "ML"}
                """;

        mockMvc.perform(post("/api/v1/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(INGREDIENT_ID.toString()))
                .andExpect(jsonPath("$.nameLt").value(INGREDIENT_NAME))
                .andExpect(jsonPath("$.unit").value("ML"));
    }

    @Test
    void test_shouldReturn400WhenCreateNameBlank() throws Exception {
        String body = """
                {"nameLt": "", "unit": "ML"}
                """;

        mockMvc.perform(post("/api/v1/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn400WhenCreateNameTooLong() throws Exception {
        String tooLong = "a".repeat(101);
        String body = String.format("{\"nameLt\": \"%s\", \"unit\": \"ML\"}", tooLong);

        mockMvc.perform(post("/api/v1/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn400WhenCreateUnitNull() throws Exception {
        String body = """
                {"nameLt": "Pienas", "unit": null}
                """;

        mockMvc.perform(post("/api/v1/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn409WhenCreateDuplicate() throws Exception {
        when(ingredientService.create(any(), any(IngredientRequestDto.class)))
                .thenThrow(new ConflictException("Ingredient 'Pienas' already exists"));

        String body = """
                {"nameLt": "Pienas", "unit": "ML"}
                """;

        mockMvc.perform(post("/api/v1/ingredients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    // ---------- PUT /api/v1/ingredients/{id} ----------

    @Test
    void test_shouldReturn200WhenUpdateSucceeds() throws Exception {
        IngredientResponseDto response = new IngredientResponseDto(INGREDIENT_ID, "Pienelis", Unit.ML);
        when(ingredientService.update(any(), eq(INGREDIENT_ID), any(IngredientRequestDto.class))).thenReturn(response);

        String body = """
                {"nameLt": "Pienelis", "unit": "ML"}
                """;

        mockMvc.perform(put("/api/v1/ingredients/" + INGREDIENT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(INGREDIENT_ID.toString()))
                .andExpect(jsonPath("$.nameLt").value("Pienelis"))
                .andExpect(jsonPath("$.unit").value("ML"));
    }

    @Test
    void test_shouldReturn404WhenUpdateNotFound() throws Exception {
        when(ingredientService.update(any(), eq(INGREDIENT_ID), any(IngredientRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Ingredient not found"));

        String body = """
                {"nameLt": "Pienas", "unit": "ML"}
                """;

        mockMvc.perform(put("/api/v1/ingredients/" + INGREDIENT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_shouldReturn409WhenUpdateRenameToExistingName() throws Exception {
        when(ingredientService.update(any(), eq(INGREDIENT_ID), any(IngredientRequestDto.class)))
                .thenThrow(new ConflictException("Ingredient 'Sviestas' already exists"));

        String body = """
                {"nameLt": "Sviestas", "unit": "ML"}
                """;

        mockMvc.perform(put("/api/v1/ingredients/" + INGREDIENT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void test_shouldReturn400WhenUpdateNameBlank() throws Exception {
        String body = """
                {"nameLt": "", "unit": "ML"}
                """;

        mockMvc.perform(put("/api/v1/ingredients/" + INGREDIENT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------- DELETE /api/v1/ingredients/{id} ----------

    @Test
    void test_shouldReturn204WhenDeleteSucceeds() throws Exception {
        mockMvc.perform(delete("/api/v1/ingredients/" + INGREDIENT_ID).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_shouldReturn404WhenDeleteNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Ingredient not found"))
                .when(ingredientService).delete(any(), eq(INGREDIENT_ID));

        mockMvc.perform(delete("/api/v1/ingredients/" + INGREDIENT_ID).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_shouldReturn409WhenDeleteUsedInRecipes() throws Exception {
        doThrow(new ConflictException("Ingredient is used in recipes and cannot be deleted."))
                .when(ingredientService).delete(any(), eq(INGREDIENT_ID));

        mockMvc.perform(delete("/api/v1/ingredients/" + INGREDIENT_ID).with(csrf()))
                .andExpect(status().isConflict());
    }
}
