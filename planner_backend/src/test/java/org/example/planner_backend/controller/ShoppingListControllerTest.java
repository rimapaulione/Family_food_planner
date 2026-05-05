package org.example.planner_backend.controller;

import org.example.planner_backend.dto.shopping.ShoppingItemManualDto;
import org.example.planner_backend.dto.shopping.ShoppingItemPlanDto;
import org.example.planner_backend.dto.shopping.ShoppingListResponseDto;
import org.example.planner_backend.service.JwtService;
import org.example.planner_backend.service.ShoppingListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShoppingListController.class)
@WithMockUser(username = "user@example.com", roles = "USER")
class ShoppingListControllerTest {

    private static final UUID INGREDIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDate WEEK_START = LocalDate.of(2026, 5, 4);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 5, 10);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShoppingListService shoppingListService;

    @MockitoBean
    private JwtService jwtService;

    private ShoppingListResponseDto sampleResponse() {
        ShoppingItemPlanDto item = new ShoppingItemPlanDto(
                INGREDIENT_ID, "Pasta", "G",
                new BigDecimal("500"), new BigDecimal("500"), false);
        return new ShoppingListResponseDto(WEEK_START, WEEK_END, List.of(item), List.<ShoppingItemManualDto>of());
    }

    // ---------- GET /api/v1/shopping-lists/family ----------

    @Test
    void getForFamily_shouldReturn200WithList() throws Exception {
        when(shoppingListService.getForFamily(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/shopping-lists/family")
                        .param("weekStart", WEEK_START.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").value(WEEK_START.toString()))
                .andExpect(jsonPath("$.items[0].name").value("Pasta"))
                .andExpect(jsonPath("$.items[0].isBought").value(false));
    }

    @Test
    void getForFamily_shouldReturn400WhenWeekStartMissing() throws Exception {
        mockMvc.perform(get("/api/v1/shopping-lists/family"))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /api/v1/shopping-lists/checks ----------

    @Test
    void markBought_shouldReturn204() throws Exception {
        String body = """
                {"weekStart": "%s", "ingredientId": "%s", "quantity": 100}
                """.formatted(WEEK_START, INGREDIENT_ID);

        mockMvc.perform(post("/api/v1/shopping-lists/checks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void markBought_shouldReturn400WhenIngredientIdMissing() throws Exception {
        String body = """
                {"weekStart": "%s", "quantity": 100}
                """.formatted(WEEK_START);

        mockMvc.perform(post("/api/v1/shopping-lists/checks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------- DELETE /api/v1/shopping-lists/checks ----------

    @Test
    void markUnbought_shouldReturn204() throws Exception {
        String body = """
                {"weekStart": "%s", "ingredientId": "%s", "quantity": 100}
                """.formatted(WEEK_START, INGREDIENT_ID);

        mockMvc.perform(delete("/api/v1/shopping-lists/checks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }
}
