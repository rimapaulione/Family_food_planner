package org.example.planner_backend.dto.family;


import org.example.planner_backend.model.entity.MealServings;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FamilyResponseDto(
        UUID id,
        String name,
        DayOfWeek shoppingDay,
        MealServings defaultWeekdayServings,
        MealServings defaultWeekendServings,
        Integer noRepeatRecipeDays,
        boolean isSetupCompleted,
        List<FamilyMemberDto> members
) {
}