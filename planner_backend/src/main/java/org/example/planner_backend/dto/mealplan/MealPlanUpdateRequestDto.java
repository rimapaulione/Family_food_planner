package org.example.planner_backend.dto.mealplan;

import jakarta.validation.constraints.NotNull;
import org.example.planner_backend.model.enums.PlanStatus;

public record MealPlanUpdateRequestDto(
        @NotNull PlanStatus status
) {
}
