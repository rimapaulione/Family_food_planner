package org.example.planner_backend.dto.family;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.planner_backend.model.entity.MealServings;

import java.time.DayOfWeek;

public record FamilySettingsRequestDto(
        @NotBlank @Size(min = 2, max = 100)
        String name,

        @NotNull
        DayOfWeek shoppingDay,

        @NotNull @Valid
        MealServings defaultWeekdayServings,

        @NotNull @Valid
        MealServings defaultWeekendServings,

        boolean isSetupCompleted
) {
}
