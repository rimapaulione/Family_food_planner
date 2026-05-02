package org.example.planner_backend.dto.mealplan;

public record MealPlanWindowResponseDto(
        MealPlanResponseDto currentWeek,
        MealPlanResponseDto nextWeek
) {
}
