package org.example.planner_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.shopping.CreateManualItemRequestDto;
import org.example.planner_backend.dto.shopping.MarkBoughtPlanRequestDto;
import org.example.planner_backend.dto.shopping.ShoppingItemManualDto;
import org.example.planner_backend.dto.shopping.ShoppingItemPlanDto;
import org.example.planner_backend.dto.shopping.ShoppingListResponseDto;
import org.example.planner_backend.dto.shopping.UpdateManualItemBoughtRequestDto;
import org.example.planner_backend.exception.BadRequestException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.mapper.ShoppingListManualHistoryMapper;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.model.entity.MealPlan;
import org.example.planner_backend.model.entity.MealSlot;
import org.example.planner_backend.model.entity.Recipe;
import org.example.planner_backend.model.entity.RecipeIngredient;
import org.example.planner_backend.model.entity.ShoppingListManualHistory;
import org.example.planner_backend.model.entity.ShoppingListPlanHistory;
import org.example.planner_backend.model.enums.Unit;
import org.example.planner_backend.repository.IngredientRepository;
import org.example.planner_backend.repository.MealPlanRepository;
import org.example.planner_backend.repository.ShoppingListManualHistoryRepository;
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

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final MealPlanRepository mealPlanRepository;
    private final ShoppingListPlanHistoryRepository planHistoryRepository;
    private final ShoppingListManualHistoryRepository manualHistoryRepository;
    private final ShoppingListManualHistoryMapper manualHistoryMapper;
    private final IngredientRepository ingredientRepository;
    private final FamilyResolver familyResolver;

    @Transactional(readOnly = true)
    public ShoppingListResponseDto getForFamily(final String email, final LocalDate weekStart) {
        Family family = familyResolver.getFamilyByEmail(email);
        LocalDate weekEnd = weekStart.plusDays(6);

        Optional<MealPlan> plan = mealPlanRepository.findByFamilyIdAndStartDate(family.getId(), weekStart);

        List<ShoppingListPlanHistory> boughtPlanItems = planHistoryRepository.findByFamilyIdAndWeekStart(family.getId(), weekStart);

        Map<UUID, BigDecimal> boughtQty = new HashMap<>();
        for (ShoppingListPlanHistory boughtPlanItem : boughtPlanItems) {
            boughtQty.put(boughtPlanItem.getIngredient().getId(), boughtPlanItem.getQuantity());
        }

        Map<UUID, AggregatedItem> neededByIngredient = plan
                .map(p -> this.aggregatePlanItems(p, family))
                .orElseGet(LinkedHashMap::new);

        List<ShoppingItemPlanDto> items = new ArrayList<>();

        for (AggregatedItem needed : neededByIngredient.values()) {
            BigDecimal bought = boughtQty.get(needed.ingredientId());
            if (bought != null && bought.compareTo(needed.quantity()) < 0) {
                items.add(new ShoppingItemPlanDto(
                        needed.ingredientId(), needed.name(), needed.unit(),
                        bought, bought, true));
                items.add(new ShoppingItemPlanDto(
                        needed.ingredientId(), needed.name(), needed.unit(),
                        needed.quantity().subtract(bought), needed.quantity(), false));
            } else {
                boolean isBought = bought != null;
                BigDecimal displayQty = isBought ? bought : needed.quantity();
                items.add(new ShoppingItemPlanDto(
                        needed.ingredientId(), needed.name(), needed.unit(),
                        displayQty, needed.quantity(), isBought));
            }
        }

        for (ShoppingListPlanHistory purchase : boughtPlanItems) {
            UUID ingredientId = purchase.getIngredient().getId();
            if (!neededByIngredient.containsKey(ingredientId)) {
                Ingredient ing = purchase.getIngredient();
                items.add(new ShoppingItemPlanDto(
                        ingredientId,
                        ing.getNameLt(),
                        ing.getUnit().name(),
                        purchase.getQuantity(),
                        purchase.getQuantity(),
                        true));
            }
        }

        List<ShoppingItemManualDto> manualItems = manualHistoryRepository
                .findByFamilyIdAndWeekStart(family.getId(), weekStart)
                .stream()
                .map(manualHistoryMapper::toDto)
                .toList();

        return new ShoppingListResponseDto(weekStart, weekEnd, items, manualItems);
    }

    @Transactional
    public void markBought(final String email, final MarkBoughtPlanRequestDto request) {
        this.validateWeekStart(request.weekStart());
        Family family = familyResolver.getFamilyByEmail(email);
        Ingredient ingredient = ingredientRepository
                .findByIdAndFamilyId(request.ingredientId(), family.getId())
                .orElseThrow(() -> new UnauthorizedException("Ingredient not in your family"));

        planHistoryRepository
                .findByFamilyIdAndWeekStartAndIngredientId(family.getId(), request.weekStart(), request.ingredientId())
                .ifPresentOrElse(
                        existing -> existing.setQuantity(request.quantity()),
                        () -> planHistoryRepository.save(ShoppingListPlanHistory.builder()
                                .family(family)
                                .weekStart(request.weekStart())
                                .ingredient(ingredient)
                                .quantity(request.quantity())
                                .build())
                );
    }

    @Transactional
    public void markUnbought(final String email, final MarkBoughtPlanRequestDto request) {
        this.validateWeekStart(request.weekStart());
        Family family = familyResolver.getFamilyByEmail(email);
        planHistoryRepository
                .findByFamilyIdAndWeekStartAndIngredientId(family.getId(), request.weekStart(), request.ingredientId())
                .ifPresent(planHistoryRepository::delete);
    }

    @Transactional
    public ShoppingItemManualDto createManualItem(final String email, final CreateManualItemRequestDto request) {
        this.validateWeekStart(request.weekStart());
        Family family = familyResolver.getFamilyByEmail(email);

        ShoppingListManualHistory saved = manualHistoryRepository.save(
                ShoppingListManualHistory.builder()
                        .family(family)
                        .weekStart(request.weekStart())
                        .name(request.name())
                        .unit(request.unit() != null ? request.unit() : Unit.VNT)
                        .quantity(request.quantity() != null ? request.quantity() : BigDecimal.ONE)
                        .build());

        return manualHistoryMapper.toDto(saved);
    }

    @Transactional
    public void updateManualItemBought(final String email, final UUID itemId, final UpdateManualItemBoughtRequestDto request) {
        Family family = familyResolver.getFamilyByEmail(email);
        ShoppingListManualHistory item = manualHistoryRepository
                .findByIdAndFamilyId(itemId, family.getId())
                .orElseThrow(() -> new UnauthorizedException("Item not in your family"));
        item.setBought(request.isBought());
    }

    private void validateWeekStart(final LocalDate weekStart) {
        LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate nextMonday = currentMonday.plusWeeks(1);
        if (!weekStart.equals(currentMonday) && !weekStart.equals(nextMonday)) {
            throw new BadRequestException("weekStart must be current or next week's Monday");
        }
    }

    private Map<UUID, AggregatedItem> aggregatePlanItems(final MealPlan plan, final Family family) {
        Map<UUID, AggregatedItem> agg = new LinkedHashMap<>();
        for (MealSlot slot : plan.getSlots()) {
            Recipe recipe = slot.getRecipe();
            if (recipe == null) continue;
            Integer needed = ServingsResolver.resolveServings(slot, family);
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

    private record AggregatedItem(UUID ingredientId, String name, String unit, BigDecimal quantity) {
        AggregatedItem merge(final AggregatedItem other) {
            return new AggregatedItem(ingredientId, name, unit, quantity.add(other.quantity));
        }
    }
}
