package org.example.planner_backend.dto.mealplan;

import org.example.planner_backend.model.enums.MealType;

import java.time.LocalDate;
import java.util.UUID;

public record MealSlotResponseDto(
        UUID id,
        LocalDate date,
        MealType mealType,
        UUID recipeId,
        String recipeName,
        Integer servings
) {
}
