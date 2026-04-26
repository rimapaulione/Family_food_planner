package org.example.planner_backend.model.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MealServings(
        @Min(1) @Max(50) Integer breakfast,
        @Min(1) @Max(50) Integer lunch,
        @Min(1) @Max(50) Integer dinner
) {
}
