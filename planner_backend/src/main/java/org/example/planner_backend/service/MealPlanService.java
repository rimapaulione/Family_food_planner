package org.example.planner_backend.service;


import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.mealplan.MealPlanResponseDto;
import org.example.planner_backend.dto.mealplan.MealPlanUpdateRequestDto;
import org.example.planner_backend.dto.mealplan.MealPlanWindowResponseDto;
import org.example.planner_backend.dto.mealplan.MealSlotResponseDto;
import org.example.planner_backend.dto.mealplan.MealSlotUpdateRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.mapper.MealPlanMapper;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.MealPlan;
import org.example.planner_backend.model.entity.MealServings;
import org.example.planner_backend.model.entity.MealSlot;
import org.example.planner_backend.model.entity.Recipe;
import org.example.planner_backend.model.entity.Tag;
import org.example.planner_backend.model.enums.MealType;
import org.example.planner_backend.model.enums.PlanStatus;
import org.example.planner_backend.repository.MealPlanRepository;
import org.example.planner_backend.repository.MealSlotRepository;
import org.example.planner_backend.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MealPlanService {


    private final MealPlanRepository mealPlanRepository;
    private final MealSlotRepository mealSlotRepository;
    private final RecipeRepository recipeRepository;
    private final MealPlanMapper mealPlanMapper;
    private final FamilyResolver familyResolver;
    private final Random random = new Random();


    @Transactional
    public MealPlanWindowResponseDto getCurrentAndNext(final String email) {

        Family family = familyResolver.getFamilyByEmail(email);
        LocalDate startOfCurrentWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate startOfNextWeek = startOfCurrentWeek.plusWeeks(1);

        MealPlan currentWeekPlan = this.getOrCreatePlan(family, startOfCurrentWeek);
        MealPlan nextWeekPlan = this.getOrCreatePlan(family, startOfNextWeek);

        return new MealPlanWindowResponseDto(
                mealPlanMapper.toPlanDto(currentWeekPlan),
                mealPlanMapper.toPlanDto(nextWeekPlan)
        );
    }

    @Transactional
    public MealPlanResponseDto updatePlan(final String email, final UUID id, final MealPlanUpdateRequestDto request) {
        MealPlan plan = mealPlanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Meal plan not found"));

        AppUser admin = familyResolver.getAdminUser(email);
        if (!plan.getFamily().getId().equals(admin.getFamily().getId())) {
            throw new UnauthorizedException("Plan not in your family");
        }
        plan.setStatus(request.status());
        return mealPlanMapper.toPlanDto(plan);
    }

    @Transactional
    public MealSlotResponseDto updateSlot(final String email, final UUID id, final MealSlotUpdateRequestDto request) {
        Family family = familyResolver.getFamilyByEmail(email);
        MealSlot slot = mealSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal slot not found"));

        if (!slot.getMealPlan().getFamily().getId().equals(family.getId())) {
            throw new UnauthorizedException("Slot not in your family");
        }
        if (slot.getMealPlan().getStatus() == PlanStatus.LOCKED) {
            throw new ConflictException("Cannot edit a locked plan");
        }

        if (request.recipeId() != null) {
            Recipe recipe = recipeRepository.findByIdAndFamilyId(request.recipeId(), family.getId())
                    .orElseThrow(() -> new UnauthorizedException("Recipe not in your family"));
            slot.setRecipe(recipe);
        } else {
            slot.setRecipe(null);
        }
        slot.setServings(request.servings());

        return mealPlanMapper.toSlotDto(slot);
    }


    @Transactional
    public MealPlanResponseDto autoFillPlan(final String email, final UUID planId) {
        AppUser admin = familyResolver.getAdminUser(email);
        Family family = admin.getFamily();
        MealPlan plan = mealPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found"));
        if (!plan.getFamily().getId().equals(family.getId())) {
            throw new UnauthorizedException("Plan not in your family");
        }
        if (plan.getStatus() == PlanStatus.LOCKED) {
            throw new ConflictException("Cannot auto-fill a locked plan");
        }

        List<Recipe> recipes = recipeRepository.findByFamilyId(family.getId());
        if (recipes.isEmpty()) {
            return mealPlanMapper.toPlanDto(plan);
        }

        LocalDate today = LocalDate.now();
        LocalDate noRepeatSince = today.minusDays(family.getNoRepeatRecipeDays());


        Set<UUID> alreadyPlannedRecipes = mealPlanRepository
                .findByFamilyIdAndStartDateGreaterThanEqual(family.getId(), noRepeatSince)
                .stream()
                .flatMap(p -> p.getSlots().stream())
                .map(MealSlot::getRecipe)
                .filter(Objects::nonNull)
                .map(Recipe::getId)
                .collect(Collectors.toCollection(HashSet::new));

        String season = AutoFillConstants.currentSeason(today.getMonth());
        Instant newRecipeCutoff = Instant.now().minus(AutoFillConstants.NEW_RECIPE_DAYS, ChronoUnit.DAYS);

        Map<UUID, UUID> consumerByProducer = new HashMap<>();
        for (Recipe r : recipes) {
            if (r.getLeftoverRecipe() != null) {
                consumerByProducer.put(r.getLeftoverRecipe().getId(), r.getId());
            }
        }

        Map<MealType, UUID> hintsForNextDay = new HashMap<>();
        Map<MealType, UUID> hintsForCurrentDay = new HashMap<>();

        LocalDate currentDay = null;

        List<MealSlot> sortedSlots = plan.getSlots().stream()
                .sorted(Comparator.comparing(MealSlot::getDate)
                        .thenComparing(MealSlot::getMealType))
                .toList();

        for (MealSlot slot : sortedSlots) {
            if (!slot.getDate().equals(currentDay)) {
                hintsForCurrentDay = new HashMap<>(hintsForNextDay);
                hintsForNextDay.clear();
                currentDay = slot.getDate();
            }

            if (slot.getRecipe() != null) {
                this.applyLeftoverHint(slot, hintsForNextDay, consumerByProducer);
                continue;
            }

            if (slot.getDate().isBefore(today)) continue;

            Integer servings = this.effectiveServings(slot, family);
            if (servings == null) continue;

            Integer maxMinutes = this.isWeekend(slot.getDate())
                    ? family.getMaxWeekendCookingMinutes()
                    : family.getMaxWeekdayCookingMinutes();

            Recipe picked = this.pickForSlot(
                    recipes, slot.getMealType(), maxMinutes, alreadyPlannedRecipes,
                    hintsForCurrentDay.get(slot.getMealType()), season, newRecipeCutoff);

            if (picked != null) {
                slot.setRecipe(picked);
                alreadyPlannedRecipes.add(picked.getId());
                this.applyLeftoverHint(slot, hintsForNextDay, consumerByProducer);
            }
        }

        return mealPlanMapper.toPlanDto(plan);
    }

    private void applyLeftoverHint(final MealSlot slot,
                                   final Map<MealType, UUID> hints,
                                   final Map<UUID, UUID> consumerByProducer) {
        Recipe r = slot.getRecipe();
        if (r == null) return;
        UUID consumer = consumerByProducer.get(r.getId());
        if (consumer != null) {
            hints.put(slot.getMealType(), consumer);
        }
    }

    private Recipe pickForSlot(final List<Recipe> recipes, final MealType mealType,
                               final Integer maxMinutes, final Set<UUID> alreadyPlanned,
                               final UUID leftoverHint, final String season,
                               final Instant newRecipeCutoff) {

        Set<String> allowedCats = AutoFillConstants.CATEGORY_FOR_MEAL.getOrDefault(mealType, Set.of());

        List<Scored> candidates = this.scoreFiltered(recipes, allowedCats, maxMinutes, alreadyPlanned, leftoverHint, season, newRecipeCutoff)
                .filter(s -> s.score() > AutoFillConstants.SCORE_REPEAT_PENALTY)
                .sorted(Comparator.comparingInt(Scored::score).reversed())
                .toList();
        if (!candidates.isEmpty()) return this.pickFromTopN(candidates);

        candidates = this.scoreFiltered(recipes, allowedCats, null, alreadyPlanned, leftoverHint, season, newRecipeCutoff)
                .filter(s -> s.score() > AutoFillConstants.SCORE_REPEAT_PENALTY)
                .sorted(Comparator.comparingInt(Scored::score).reversed())
                .toList();
        if (!candidates.isEmpty()) return this.pickFromTopN(candidates);

        candidates = this.scoreFiltered(recipes, null, null, alreadyPlanned, leftoverHint, season, newRecipeCutoff)
                .filter(s -> s.score() > AutoFillConstants.SCORE_REPEAT_PENALTY)
                .sorted(Comparator.comparingInt(Scored::score).reversed())
                .toList();
        if (!candidates.isEmpty()) return this.pickFromTopN(candidates);

        candidates = this.scoreFiltered(recipes, null, null, alreadyPlanned, leftoverHint, season, newRecipeCutoff)
                .sorted(Comparator.comparingInt(Scored::score).reversed())
                .toList();
        return candidates.isEmpty() ? null : this.pickFromTopN(candidates);
    }

    private Stream<Scored> scoreFiltered(final List<Recipe> recipes, final Set<String> allowedCats,
                                         final Integer maxMinutes, final Set<UUID> alreadyPlanned,
                                         final UUID leftoverHint, final String season,
                                         final Instant newRecipeCutoff) {
        return recipes.stream()
                .filter(r -> allowedCats == null
                        || (r.getCategory() != null && allowedCats.contains(r.getCategory().getName())))
                .filter(r -> maxMinutes == null || r.getCookingTimeMinutes() <= maxMinutes)
                .map(r -> this.score(r, alreadyPlanned, leftoverHint, season, newRecipeCutoff));
    }

    private Recipe pickFromTopN(final List<Scored> sorted) {
        int n = Math.min(AutoFillConstants.TOP_N_FOR_RANDOM, sorted.size());
        return sorted.get(random.nextInt(n)).recipe();
    }

    private Scored score(final Recipe r, final Set<UUID> alreadyPlanned,
                         final UUID leftoverHint, final String season,
                         final Instant newRecipeCutoff) {
        int s = 0;
        boolean isLeftoverConsumer = leftoverHint != null && leftoverHint.equals(r.getId());
        if (alreadyPlanned.contains(r.getId()) && !isLeftoverConsumer) {
            s += AutoFillConstants.SCORE_REPEAT_PENALTY;
        }
        Set<String> tagNames = r.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
        if (tagNames.contains(AutoFillConstants.TAG_KID_FAVORITE)) s += AutoFillConstants.SCORE_KID_FAVORITE;
        boolean hasSeason = tagNames.stream().anyMatch(AutoFillConstants.SEASON_TAGS::contains);
        if (hasSeason && tagNames.contains(season)) s += AutoFillConstants.SCORE_SEASON_MATCH;
        else if (!hasSeason) s += AutoFillConstants.SCORE_NO_SEASON;
        if (Boolean.TRUE.equals(r.getIsFavorite())) s += AutoFillConstants.SCORE_FAVORITE;
        if (isLeftoverConsumer) s += AutoFillConstants.SCORE_LEFTOVER;
        if (r.getCreatedAt() != null && r.getCreatedAt().isAfter(newRecipeCutoff)) {
            s += AutoFillConstants.SCORE_NEW_RECIPE;
        }
        return new Scored(r, s);
    }

    private Integer effectiveServings(final MealSlot slot, final Family family) {
        if (slot.getServings() != null) return slot.getServings();
        boolean weekend = this.isWeekend(slot.getDate());
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

    private record Scored(Recipe recipe, int score) {
    }

    private MealPlan getOrCreatePlan(final Family family, final LocalDate startOfWeek) {
        return mealPlanRepository.findByFamilyIdAndStartDate(family.getId(), startOfWeek)
                .orElseGet(() -> this.createPlan(family, startOfWeek));
    }

    private MealPlan createPlan(final Family family, final LocalDate startOfWeek) {
        MealPlan plan = mealPlanRepository.save(MealPlan.builder()
                .family(family)
                .startDate(startOfWeek)
                .endDate(startOfWeek.plusDays(6))
                .status(PlanStatus.DRAFT)
                .build());
        List<MealSlot> slots = mealSlotRepository.saveAll(this.generateMealSlots(plan));
        plan.setSlots(slots);
        return plan;
    }

    private List<MealSlot> generateMealSlots(final MealPlan mealPlan) {
        List<MealSlot> slots = new ArrayList<>();
        for (int offset = 0; offset < 7; offset++) {
            LocalDate date = mealPlan.getStartDate().plusDays(offset);
            slots.add(this.buildSlot(mealPlan, date, MealType.BREAKFAST));
            slots.add(this.buildSlot(mealPlan, date, MealType.LUNCH));
            slots.add(this.buildSlot(mealPlan, date, MealType.DINNER));
        }
        return slots;
    }

    private MealSlot buildSlot(final MealPlan plan, final LocalDate date, final MealType type) {
        return MealSlot.builder()
                .mealPlan(plan)
                .date(date)
                .mealType(type)
                .build();
    }
}

