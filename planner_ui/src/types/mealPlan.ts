export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

export const MEAL_TYPE = {
    BREAKFAST: 'BREAKFAST',
    LUNCH: 'LUNCH',
    DINNER: 'DINNER',
} as const;

export type PlanStatus = 'DRAFT' | 'LOCKED';

export const PLAN_STATUS = {
    DRAFT: 'DRAFT',
    LOCKED: 'LOCKED',
} as const;

export interface MealSlot {
    id: string;
    date: string;
    mealType: MealType;
    recipeId: string | null;
    recipeName: string | null;
    cookingTimeMinutes: number | null;
    recipeDefaultServing: number | null;
    servings: number | null;
}

export interface MealPlan {
    id: string;
    startDate: string;
    endDate: string;
    status: PlanStatus;
    slots: MealSlot[];
}

export interface MealPlanWindow {
    currentWeek: MealPlan;
    nextWeek: MealPlan;
}

export interface MealSlotUpdateRequest {
    recipeId: string | null;
    servings: number | null;
}

export interface MealPlanUpdateRequest {
    status: PlanStatus;
}
