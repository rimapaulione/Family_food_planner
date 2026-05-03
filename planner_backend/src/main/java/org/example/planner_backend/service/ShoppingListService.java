package org.example.planner_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.shopping.ShoppingItemDto;
import org.example.planner_backend.dto.shopping.ShoppingListResponseDto;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.model.entity.MealPlan;
import org.example.planner_backend.model.entity.MealServings;
import org.example.planner_backend.model.entity.MealSlot;
import org.example.planner_backend.model.entity.Recipe;
import org.example.planner_backend.model.entity.RecipeIngredient;
import org.example.planner_backend.model.entity.ShoppingListPlanHistory;
import org.example.planner_backend.repository.MealPlanRepository;
import org.example.planner_backend.repository.ShoppingListPlanHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final MealPlanRepository mealPlanRepository;
    private final ShoppingListPlanHistoryRepository planHistoryRepository;
    private final FamilyResolver familyResolver;

    @Transactional(readOnly = true)
    public ShoppingListResponseDto getForFamily(final String email, final LocalDate weekStart) {
        Family family = familyResolver.getFamilyByEmail(email);
        LocalDate weekEnd = weekStart.plusDays(6);

        Optional<MealPlan> plan = mealPlanRepository.findWithIngredientsByFamilyIdAndStartDate(family.getId(), weekStart);

        List<ShoppingListPlanHistory> historyRows = planHistoryRepository.findByFamilyIdAndWeekStart(family.getId(), weekStart);

        Map<UUID, BigDecimal> boughtQty = new HashMap<>();
        for (ShoppingListPlanHistory h : historyRows) {
            boughtQty.put(h.getIngredient().getId(), h.getQuantity());
        }

        Map<UUID, AggregatedItem> aggregated = plan
                .map(p -> aggregatePlanItems(p, family))
                .orElseGet(LinkedHashMap::new);

        List<ShoppingItemDto> items = new ArrayList<>();
        for (AggregatedItem agg : aggregated.values()) {
            BigDecimal bought = boughtQty.get(agg.ingredientId());
            boolean isBought = bought != null && bought.compareTo(agg.quantity()) == 0;
            items.add(new ShoppingItemDto(agg.ingredientId(), agg.name(), agg.unit(), agg.quantity(), isBought));
        }

        for (ShoppingListPlanHistory row : historyRows) {
            UUID ingredientId = row.getIngredient().getId();
            if (!aggregated.containsKey(ingredientId)) {
                Ingredient ing = row.getIngredient();
                items.add(new ShoppingItemDto(
                        ingredientId,
                        ing.getNameLt(),
                        ing.getUnit().name(),
                        row.getQuantity(),
                        true));
            }
        }

        return new ShoppingListResponseDto(weekStart, weekEnd, items);
    }

    private Map<UUID, AggregatedItem> aggregatePlanItems(final MealPlan plan, final Family family) {
        Map<UUID, AggregatedItem> agg = new LinkedHashMap<>();
        for (MealSlot slot : plan.getSlots()) {
            Recipe recipe = slot.getRecipe();
            if (recipe == null) continue;
            Integer needed = effectiveServings(slot, family);
            if (needed == null) continue;
            int batches = Math.max(1, (int) Math.ceil((double) needed / recipe.getDefaultServing()));
            BigDecimal multiplier = BigDecimal.valueOf(batches);
            for (RecipeIngredient ri : recipe.getIngredients()) {
                Ingredient ing = ri.getIngredient();
                BigDecimal totalQty = ri.getQuantity().multiply(multiplier);
                agg.merge(
                        ing.getId(),
                        new AggregatedItem(ing.getId(), ing.getNameLt(), ing.getUnit().name(), totalQty),
                        AggregatedItem::merge
                );
            }
        }
        return agg;
    }

    private Integer effectiveServings(final MealSlot slot, final Family family) {
        if (slot.getServings() != null) return slot.getServings();
        boolean weekend = isWeekend(slot.getDate());
        MealServings defaults = weekend ? family.getDefaultWeekendServings() : family.getDefaultWeekdayServings();
        Integer fromFamily = switch (slot.getMealType()) {
            case BREAKFAST -> defaults.breakfast();
            case LUNCH -> defaults.lunch();
            case DINNER -> defaults.dinner();
        };
        if (fromFamily != null) return fromFamily;
        return slot.getRecipe() != null ? (int) slot.getRecipe().getDefaultServing() : null;
    }

    private boolean isWeekend(final LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    private record AggregatedItem(UUID ingredientId, String name, String unit, BigDecimal quantity) {
        AggregatedItem merge(final AggregatedItem other) {
            return new AggregatedItem(ingredientId, name, unit, quantity.add(other.quantity));
        }
    }
}
