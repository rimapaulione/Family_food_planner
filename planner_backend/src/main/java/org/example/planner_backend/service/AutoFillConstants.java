package org.example.planner_backend.service;

import org.example.planner_backend.model.enums.MealType;

import java.time.Month;
import java.util.Map;
import java.util.Set;

public final class AutoFillConstants {

    public static final Map<MealType, Set<String>> CATEGORY_FOR_MEAL = Map.of(
            MealType.BREAKFAST, Set.of("breakfast"),
            MealType.LUNCH, Set.of("main_course", "soup", "salad"),
            MealType.DINNER, Set.of("main_course", "soup", "salad", "side_dish")
    );

    public static final String TAG_KID_FAVORITE = "kid-favorite";
    public static final Set<String> SEASON_TAGS = Set.of("spring", "summer", "autumn", "winter");

    public static final int SCORE_REPEAT_PENALTY = -50;
    public static final int SCORE_KID_FAVORITE = 5;
    public static final int SCORE_SEASON_MATCH = 10;
    public static final int SCORE_NO_SEASON = 3;
    public static final int SCORE_FAVORITE = 2;
    public static final int SCORE_LEFTOVER = 30;

    public static final int TOP_N_FOR_RANDOM = 3;

    public static String currentSeason(final Month m) {
        return switch (m) {
            case MARCH, APRIL, MAY -> "spring";
            case JUNE, JULY, AUGUST -> "summer";
            case SEPTEMBER, OCTOBER, NOVEMBER -> "autumn";
            case DECEMBER, JANUARY, FEBRUARY -> "winter";
        };
    }

    private AutoFillConstants() {
    }
}
