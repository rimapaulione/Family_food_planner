package org.example.planner_backend.controller;

import org.example.planner_backend.dto.category.CategoryResponseDto;
import org.example.planner_backend.dto.recipe.RecipeListResponseDto;
import org.example.planner_backend.dto.recipe.RecipeRequestDto;
import org.example.planner_backend.dto.recipe.RecipeResponseDto;
import org.example.planner_backend.dto.tag.TagResponseDto;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.service.JwtService;
import org.example.planner_backend.service.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
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


@WebMvcTest(RecipeController.class)
@WithMockUser(username = "user@example.com", roles = "USER")
class RecipeControllerTest {
    private static final UUID RECIPE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String RECIPE_NAME = "Blynai";
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    RecipeService recipeService;

    @MockitoBean
    private JwtService jwtService;

    // ---------- GET /api/v1/recipes ----------

    @Test
    void test_shouldReturn200WithRecipeListWhenNoSearch() throws Exception {
        when(recipeService.getAllWithIngredients(any(), eq(null))).thenReturn(List.of(sampleRecipeListResponse()));

        mockMvc.perform(get("/api/v1/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(RECIPE_ID.toString()))
                .andExpect(jsonPath("$[0].name").value(RECIPE_NAME))
                .andExpect(jsonPath("$[0].category.name").value("Pusryčiai"))
                .andExpect(jsonPath("$[0].tags[0].name").value("Greiti"))
                .andExpect(jsonPath("$[0].ingredientCount").value(5));
    }

    @Test
    void test_shouldReturn200WithRecipeListWhenSearchProvided() throws Exception {
        when(recipeService.getAllWithIngredients(any(), eq("bly"))).thenReturn(List.of(sampleRecipeListResponse()));

        mockMvc.perform(get("/api/v1/recipes").param("search", "bly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(RECIPE_ID.toString()));
    }

    @Test
    void test_shouldReturn400WhenSearchTooLong() throws Exception {
        String tooLong = "a".repeat(51);

        mockMvc.perform(get("/api/v1/recipes").param("search", tooLong))
                .andExpect(status().isBadRequest());
    }


    // ---------- GET /api/v1/recipes/{id} ----------

    @Test
    void test_shouldReturn200WithRecipeWhenGetById() throws Exception {
        when(recipeService.getById(any(), eq(RECIPE_ID))).thenReturn(sampleRecipeResponse());

        mockMvc.perform(get("/api/v1/recipes/" + RECIPE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(RECIPE_ID.toString()))
                .andExpect(jsonPath("$.name").value(RECIPE_NAME))
                .andExpect(jsonPath("$.category.name").value("Pusryčiai"))
                .andExpect(jsonPath("$.tags[0].name").value("Greiti"))
                .andExpect(jsonPath("$.ingredients[0].ingredientName").value("Pienas"))
                .andExpect(jsonPath("$.ingredients[0].quantity").value(250))
                .andExpect(jsonPath("$.isFavorite").value(true))
                .andExpect(jsonPath("$.notes").value("Skanūs blynai"));
    }

    @Test
    void test_shouldReturn404WhenGetByIdNotFound() throws Exception {
        when(recipeService.getById(any(), eq(RECIPE_ID)))
                .thenThrow(new ResourceNotFoundException("Recipe not found"));

        mockMvc.perform(get("/api/v1/recipes/" + RECIPE_ID))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /api/v1/recipes ----------

    @Test
    void test_shouldReturn201WhenCreateSucceeds() throws Exception {
        when(recipeService.create(any(), any(RecipeRequestDto.class))).thenReturn(sampleRecipeResponse());

        String body = """
                {
                  "name": "Blynai",
                  "categoryId": 1,
                  "cookingTimeMinutes": 30,
                  "defaultServing": 4,
                  "ingredients": [
                    {"ingredientId": "44444444-4444-4444-4444-444444444444", "quantity": 250}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/recipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(RECIPE_ID.toString()))
                .andExpect(jsonPath("$.name").value(RECIPE_NAME));
    }

    @Test
    void test_shouldReturn400WhenCreateNameBlank() throws Exception {
        String body = """
                {"name": "", "categoryId": 1, "cookingTimeMinutes": 30}
                """;

        mockMvc.perform(post("/api/v1/recipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn400WhenCreateCategoryIdMissing() throws Exception {
        String body = """
                {"name": "Blynai", "cookingTimeMinutes": 30}
                """;

        mockMvc.perform(post("/api/v1/recipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn400WhenCreateIngredientQuantityInvalid() throws Exception {
        String body = """
                {
                  "name": "Blynai",
                  "categoryId": 1,
                  "cookingTimeMinutes": 30,
                  "ingredients": [
                    {"ingredientId": "44444444-4444-4444-4444-444444444444", "quantity": 0}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/recipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_shouldReturn404WhenCreateCategoryNotFound() throws Exception {
        when(recipeService.create(any(), any(RecipeRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        String body = """
                {"name": "Blynai", "categoryId": 999, "cookingTimeMinutes": 30, "defaultServing": 4}
                """;

        mockMvc.perform(post("/api/v1/recipes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /api/v1/recipes/{id} ----------

    @Test
    void test_shouldReturn200WhenUpdateSucceeds() throws Exception {
        when(recipeService.update(any(), eq(RECIPE_ID), any(RecipeRequestDto.class))).thenReturn(sampleRecipeResponse());

        String body = """
                {"name": "Blynai", "categoryId": 1, "cookingTimeMinutes": 30, "defaultServing": 4}
                """;

        mockMvc.perform(put("/api/v1/recipes/" + RECIPE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(RECIPE_ID.toString()))
                .andExpect(jsonPath("$.name").value(RECIPE_NAME));
    }

    @Test
    void test_shouldReturn404WhenUpdateNotFound() throws Exception {
        when(recipeService.update(any(), eq(RECIPE_ID), any(RecipeRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Recipe not found"));

        String body = """
                {"name": "Blynai", "categoryId": 1, "cookingTimeMinutes": 30, "defaultServing": 4}
                """;

        mockMvc.perform(put("/api/v1/recipes/" + RECIPE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_shouldReturn400WhenUpdateNameBlank() throws Exception {
        String body = """
                {"name": "", "categoryId": 1, "cookingTimeMinutes": 30}
                """;

        mockMvc.perform(put("/api/v1/recipes/" + RECIPE_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------- DELETE /api/v1/recipes/{id} ----------

    @Test
    void test_shouldReturn204WhenDeleteSucceeds() throws Exception {
        mockMvc.perform(delete("/api/v1/recipes/" + RECIPE_ID).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void test_shouldReturn404WhenDeleteNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Recipe not found"))
                .when(recipeService).delete(any(), eq(RECIPE_ID));

        mockMvc.perform(delete("/api/v1/recipes/" + RECIPE_ID).with(csrf()))
                .andExpect(status().isNotFound());
    }

    private RecipeListResponseDto sampleRecipeListResponse() {
        return new RecipeListResponseDto(
                RECIPE_ID,
                RECIPE_NAME,
                new CategoryResponseDto(1L, "Pusryčiai"),
                (short) 4,
                (short) 30,
                Set.of(new TagResponseDto(1L, "Greiti")),
                true,
                5
        );
    }

    private RecipeResponseDto sampleRecipeResponse() {
        return new RecipeResponseDto(
                RECIPE_ID,
                RECIPE_NAME,
                new CategoryResponseDto(1L, "Pusryčiai"),
                (short) 4,
                (short) 30,
                Set.of(new TagResponseDto(1L, "Greiti")),
                List.of(new RecipeResponseDto.RecipeIngredientDto(
                        UUID.fromString("33333333-3333-3333-3333-333333333333"),
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        "Pienas",
                        "ML",
                        new BigDecimal("250"))),
                null,
                true,
                "Skanūs blynai",
                Instant.parse("2026-04-20T10:00:00Z"),
                Instant.parse("2026-04-20T10:00:00Z")
        );
    }
}