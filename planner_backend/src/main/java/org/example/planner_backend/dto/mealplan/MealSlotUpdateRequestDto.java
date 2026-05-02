package org.example.planner_backend.dto.mealplan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record MealSlotUpdateRequestDto(
        UUID recipeId,
        @Min(1) @Max(50) Integer servings
) {
}
