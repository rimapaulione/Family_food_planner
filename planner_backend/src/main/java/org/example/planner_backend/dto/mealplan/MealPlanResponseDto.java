package org.example.planner_backend.dto.mealplan;

import org.example.planner_backend.model.enums.PlanStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MealPlanResponseDto(
        UUID id,
        LocalDate startDate,
        LocalDate endDate,
        PlanStatus status,
        List<MealSlotResponseDto> slots
) {
}
