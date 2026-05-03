import type {Family} from '@/types/family';
import {MEAL_TYPE, type MealPlan, type MealSlot, type MealType} from '@/types/mealPlan';

export function isMealActive(date: string, mealType: MealType, family: Family): boolean {
    const dow = new Date(date + 'T00:00:00Z').getUTCDay();
    const isWeekend = dow === 0 || dow === 6;
    const servings = isWeekend ? family.defaultWeekendServings : family.defaultWeekdayServings;
    if (mealType === MEAL_TYPE.BREAKFAST) return servings.breakfast !== null;
    if (mealType === MEAL_TYPE.LUNCH) return servings.lunch !== null;
    if (mealType === MEAL_TYPE.DINNER) return servings.dinner !== null;
    return false;
}

export function getWeekDates(startDate: string): string[] {
    const dates: string[] = [];
    const start = new Date(startDate + 'T00:00:00Z');
    for (let i = 0; i < 7; i++) {
        const d = new Date(start);
        d.setUTCDate(start.getUTCDate() + i);
        dates.push(d.toISOString().slice(0, 10));
    }
    return dates;
}

export function formatWeekdayShort(iso: string): string {
    return new Date(iso + 'T00:00:00Z').toLocaleDateString('en-US', {
        weekday: 'short',
        timeZone: 'UTC',
    });
}

export function formatDayOfMonth(iso: string): number {
    return new Date(iso + 'T00:00:00Z').getUTCDate();
}

export function totalCookingTimeForDay(date: string, plan: MealPlan): number {
    return plan.slots
        .filter((s) => s.date === date && s.cookingTimeMinutes !== null)
        .reduce((sum, s) => sum + (s.cookingTimeMinutes ?? 0), 0);
}

export function isWeekday(iso: string): boolean {
    const dow = new Date(iso + 'T00:00:00Z').getUTCDay();
    return dow >= 1 && dow <= 5;
}

export function effectiveServings(slot: MealSlot, family: Family): number | null {
    if (slot.servings !== null) return slot.servings;
    const s = isWeekday(slot.date) ? family.defaultWeekdayServings : family.defaultWeekendServings;
    if (slot.mealType === MEAL_TYPE.BREAKFAST) return s.breakfast;
    if (slot.mealType === MEAL_TYPE.LUNCH) return s.lunch;
    return s.dinner;
}
