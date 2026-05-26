package org.example.planner_backend.service;

import org.example.planner_backend.dto.shopping.MarkBoughtPlanRequestDto;
import org.example.planner_backend.dto.shopping.ShoppingItemPlanDto;
import org.example.planner_backend.dto.shopping.ShoppingListResponseDto;
import org.example.planner_backend.exception.BadRequestException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.model.entity.MealPlan;
import org.example.planner_backend.model.entity.MealServings;
import org.example.planner_backend.model.entity.MealSlot;
import org.example.planner_backend.model.entity.Recipe;
import org.example.planner_backend.model.entity.RecipeIngredient;
import org.example.planner_backend.model.entity.ShoppingListPlanHistory;
import org.example.planner_backend.model.enums.MealType;
import org.example.planner_backend.model.enums.Unit;
import org.example.planner_backend.mapper.ShoppingListManualHistoryMapper;
import org.example.planner_backend.repository.IngredientRepository;
import org.example.planner_backend.repository.MealPlanRepository;
import org.example.planner_backend.repository.ShoppingListManualHistoryRepository;
import org.example.planner_backend.repository.ShoppingListPlanHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    private static final UUID FAMILY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID INGREDIENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final String EMAIL = "user@example.com";
    private static final LocalDate CURRENT_MONDAY = LocalDate.now().with(DayOfWeek.MONDAY);

    @Mock
    private MealPlanRepository mealPlanRepository;
    @Mock
    private ShoppingListPlanHistoryRepository planHistoryRepository;
    @Mock
    private ShoppingListManualHistoryRepository manualHistoryRepository;
    @Mock
    private ShoppingListManualHistoryMapper manualHistoryMapper;
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private FamilyResolver familyResolver;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private Family family;
    private Ingredient pasta;

    @BeforeEach
    void setUp() {
        family = Family.builder()
                .id(FAMILY_ID)
                .name("Smith")
                .defaultWeekdayServings(new MealServings(3, 4, 4))
                .defaultWeekendServings(new MealServings(4, 4, 4))
                .build();
        pasta = Ingredient.builder()
                .id(INGREDIENT_ID)
                .nameLt("Pasta")
                .unit(Unit.G)
                .family(family)
                .build();
    }

    // ---------- getForFamily ----------

    @Test
    void getForFamily_returnsEmptyResponseWhenNoPlanAndNoHistory() {
        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealPlanRepository.findByFamilyIdAndStartDate(FAMILY_ID, CURRENT_MONDAY))
                .thenReturn(Optional.empty());
        when(planHistoryRepository.findByFamilyIdAndWeekStart(FAMILY_ID, CURRENT_MONDAY))
                .thenReturn(List.of());

        ShoppingListResponseDto response = shoppingListService.getForFamily(EMAIL, CURRENT_MONDAY);

        assertThat(response.weekStart()).isEqualTo(CURRENT_MONDAY);
        assertThat(response.weekEnd()).isEqualTo(CURRENT_MONDAY.plusDays(6));
        assertThat(response.items()).isEmpty();
    }

    @Test
    void getForFamily_aggregatesIngredientsFromPlanWithFullPurchase() {
        Recipe pastaRecipe = buildRecipe(4, new BigDecimal("500"));
        MealSlot slot = buildSlot(CURRENT_MONDAY, MealType.DINNER, pastaRecipe, 4);
        MealPlan plan = buildPlan(slot);

        ShoppingListPlanHistory history = ShoppingListPlanHistory.builder()
                .ingredient(pasta)
                .quantity(new BigDecimal("500"))
                .build();

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealPlanRepository.findByFamilyIdAndStartDate(FAMILY_ID, CURRENT_MONDAY))
                .thenReturn(Optional.of(plan));
        when(planHistoryRepository.findByFamilyIdAndWeekStart(FAMILY_ID, CURRENT_MONDAY))
                .thenReturn(List.of(history));

        ShoppingListResponseDto response = shoppingListService.getForFamily(EMAIL, CURRENT_MONDAY);

        assertThat(response.items()).hasSize(1);
        ShoppingItemPlanDto item = response.items().get(0);
        assertThat(item.name()).isEqualTo("Pasta");
        assertThat(item.quantity()).isEqualByComparingTo("500");
        assertThat(item.targetQuantity()).isEqualByComparingTo("500");
        assertThat(item.isBought()).isTrue();
    }

    @Test
    void getForFamily_emitsTwoRowsWhenPartialPurchase() {
        // Recipe needs 200g, only 150g bought → shortage = 50g
        Recipe pastaRecipe = buildRecipe(4, new BigDecimal("200"));
        MealSlot slot = buildSlot(CURRENT_MONDAY, MealType.DINNER, pastaRecipe, 4);
        MealPlan plan = buildPlan(slot);

        ShoppingListPlanHistory history = ShoppingListPlanHistory.builder()
                .ingredient(pasta)
                .quantity(new BigDecimal("150"))
                .build();

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealPlanRepository.findByFamilyIdAndStartDate(FAMILY_ID, CURRENT_MONDAY))
                .thenReturn(Optional.of(plan));
        when(planHistoryRepository.findByFamilyIdAndWeekStart(FAMILY_ID, CURRENT_MONDAY))
                .thenReturn(List.of(history));

        ShoppingListResponseDto response = shoppingListService.getForFamily(EMAIL, CURRENT_MONDAY);

        assertThat(response.items()).hasSize(2);
        ShoppingItemPlanDto bought = response.items().get(0);
        assertThat(bought.quantity()).isEqualByComparingTo("150");
        assertThat(bought.isBought()).isTrue();
        ShoppingItemPlanDto need = response.items().get(1);
        assertThat(need.quantity()).isEqualByComparingTo("50");
        assertThat(need.targetQuantity()).isEqualByComparingTo("200");
        assertThat(need.isBought()).isFalse();
    }

    @Test
    void getForFamily_addsGhostRowForBoughtIngredientNoLongerInPlan() {
        // No plan, but we have a purchase from earlier
        ShoppingListPlanHistory history = ShoppingListPlanHistory.builder()
                .ingredient(pasta)
                .quantity(new BigDecimal("500"))
                .build();

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealPlanRepository.findByFamilyIdAndStartDate(FAMILY_ID, CURRENT_MONDAY))
                .thenReturn(Optional.empty());
        when(planHistoryRepository.findByFamilyIdAndWeekStart(FAMILY_ID, CURRENT_MONDAY))
                .thenReturn(List.of(history));

        ShoppingListResponseDto response = shoppingListService.getForFamily(EMAIL, CURRENT_MONDAY);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualByComparingTo("500");
        assertThat(response.items().get(0).isBought()).isTrue();
    }

    // ---------- markBought ----------

    @Test
    void markBought_throwsBadRequestWhenWeekStartIsNotCurrentOrNextMonday() {
        MarkBoughtPlanRequestDto request = new MarkBoughtPlanRequestDto(
                LocalDate.of(2020, 1, 1), INGREDIENT_ID, new BigDecimal("100"));

        assertThatThrownBy(() -> shoppingListService.markBought(EMAIL, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("WeekStart");
    }

    @Test
    void markBought_throwsUnauthorizedWhenIngredientNotInFamily() {
        MarkBoughtPlanRequestDto request = new MarkBoughtPlanRequestDto(
                CURRENT_MONDAY, INGREDIENT_ID, new BigDecimal("100"));

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(ingredientRepository.findByIdAndFamilyId(INGREDIENT_ID, FAMILY_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> shoppingListService.markBought(EMAIL, request))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ---------- markUnbought ----------

    @Test
    void markUnbought_isNoOpWhenNoExistingRow() {
        MarkBoughtPlanRequestDto request = new MarkBoughtPlanRequestDto(
                CURRENT_MONDAY, INGREDIENT_ID, new BigDecimal("100"));

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(planHistoryRepository.findByFamilyIdAndWeekStartAndIngredientId(
                FAMILY_ID, CURRENT_MONDAY, INGREDIENT_ID))
                .thenReturn(Optional.empty());

        shoppingListService.markUnbought(EMAIL, request);

        verify(planHistoryRepository, never()).delete(any());
    }

    // ---------- helpers ----------

    private Recipe buildRecipe(int defaultServing, BigDecimal pastaQty) {
        return Recipe.builder()
                .id(UUID.randomUUID())
                .name("Pasta")
                .family(family)
                .defaultServing((short) defaultServing)
                .ingredients(List.of(
                        RecipeIngredient.builder()
                                .ingredient(pasta)
                                .quantity(pastaQty)
                                .build()
                ))
                .build();
    }

    private MealSlot buildSlot(LocalDate date, MealType mealType, Recipe recipe, Integer servings) {
        return MealSlot.builder()
                .id(UUID.randomUUID())
                .date(date)
                .mealType(mealType)
                .recipe(recipe)
                .servings(servings)
                .build();
    }

    private MealPlan buildPlan(MealSlot... slots) {
        return MealPlan.builder()
                .id(UUID.randomUUID())
                .family(family)
                .startDate(CURRENT_MONDAY)
                .endDate(CURRENT_MONDAY.plusDays(6))
                .slots(List.of(slots))
                .build();
    }
}
