package org.example.planner_backend.service;

import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.MealServings;
import org.example.planner_backend.model.entity.MealSlot;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class ServingsResolver {

    private ServingsResolver() {
    }

    /**
     * Resolves how many people this slot's meal should feed.
     * Walks a fallback chain: slot override → family default → recipe default → null.
     * Null means the family does not cook this meal at this time.
     */
    public static Integer resolveServings(final MealSlot slot, final Family family) {
        if (slot.getServings() != null) return slot.getServings();

        MealServings defaults = isWeekend(slot.getDate())
                ? family.getDefaultWeekendServings()
                : family.getDefaultWeekdayServings();
        Integer fromFamily = switch (slot.getMealType()) {
            case BREAKFAST -> defaults.breakfast();
            case LUNCH -> defaults.lunch();
            case DINNER -> defaults.dinner();
        };
        if (fromFamily != null) return fromFamily;

        return slot.getRecipe() != null ? (int) slot.getRecipe().getDefaultServing() : null;
    }

    public static boolean isWeekend(final LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}
