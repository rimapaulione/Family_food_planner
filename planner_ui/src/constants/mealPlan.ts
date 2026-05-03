import type {MealType} from '@/types/mealPlan';
import {MEAL_TYPE} from '@/types/mealPlan';

export const MEAL_ORDER: MealType[] = [MEAL_TYPE.BREAKFAST, MEAL_TYPE.LUNCH, MEAL_TYPE.DINNER];

export const MEAL_LABELS: Record<MealType, string> = {
    BREAKFAST: 'Breakfast',
    LUNCH: 'Lunch',
    DINNER: 'Dinner',
};

export const LONG_COOKING_MINUTES = 45;
export const LONG_DAILY_COOK_MINUTES = 60;
