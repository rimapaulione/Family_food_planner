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
import org.example.planner_backend.model.enums.MealType;
import org.example.planner_backend.model.enums.PlanStatus;
import org.example.planner_backend.repository.MealPlanRepository;
import org.example.planner_backend.repository.MealSlotRepository;
import org.example.planner_backend.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MealPlanService {


    private final MealPlanRepository mealPlanRepository;
    private final MealSlotRepository mealSlotRepository;
    private final RecipeRepository recipeRepository;
    private final MealPlanMapper mealPlanMapper;
    private final FamilyResolver familyResolver;


    @Transactional
    public MealPlanWindowResponseDto getWindow(final String email) {

        Family family = familyResolver.getFamilyByEmail(email);
        LocalDate startOfCurrentWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate startOfNextWeek = startOfCurrentWeek.plusWeeks(1);

        MealPlan currentWeekPlan = getOrCreatePlan(family, startOfCurrentWeek);
        MealPlan nextWeekPlan = getOrCreatePlan(family, startOfNextWeek);

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


    private MealPlan getOrCreatePlan(final Family family, final LocalDate startOfWeek) {
        return mealPlanRepository.findByFamilyIdAndStartDate(family.getId(), startOfWeek)
                .orElseGet(() -> createPlan(family, startOfWeek));
    }

    private MealPlan createPlan(final Family family, final LocalDate startOfWeek) {
        MealPlan plan = mealPlanRepository.save(MealPlan.builder()
                .family(family)
                .startDate(startOfWeek)
                .endDate(startOfWeek.plusDays(6))
                .status(PlanStatus.DRAFT)
                .build());
        List<MealSlot> slots = mealSlotRepository.saveAll(generateMealSlots(family, plan));
        plan.setSlots(slots);
        return plan;
    }

    private List<MealSlot> generateMealSlots(final Family family, final MealPlan mealPlan) {
        List<MealSlot> slots = new ArrayList<>();

        for (int offset = 0; offset < 7; offset++) {
            LocalDate date = mealPlan.getStartDate().plusDays(offset);

            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY;

            MealServings active = isWeekend
                    ? family.getDefaultWeekendServings()
                    : family.getDefaultWeekdayServings();

            if (active.breakfast() != null) {
                slots.add(buildSlot(mealPlan, date, MealType.BREAKFAST));
            }
            if (active.lunch() != null) {
                slots.add(buildSlot(mealPlan, date, MealType.LUNCH));
            }
            if (active.dinner() != null) {
                slots.add(buildSlot(mealPlan, date, MealType.DINNER));
            }
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

