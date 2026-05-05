import type {Role} from '@/types/auth';

export type DayOfWeek =
    | 'MONDAY'
    | 'TUESDAY'
    | 'WEDNESDAY'
    | 'THURSDAY'
    | 'FRIDAY'
    | 'SATURDAY'
    | 'SUNDAY';

export interface MealServings {
    breakfast: number | null;
    lunch: number | null;
    dinner: number | null;
}

export interface FamilyMember {
    userId: string;
    displayName: string;
    email: string;
    avatarUrl: string | null;
    role: Role;
}

export interface Family {
    id: string;
    name: string;
    shoppingDay: DayOfWeek;
    defaultWeekdayServings: MealServings;
    defaultWeekendServings: MealServings;
    noRepeatRecipeDays: number;
    maxWeekdayCookingMinutes: number | null;
    maxWeekendCookingMinutes: number | null;
    isSetupCompleted: boolean;
    members: FamilyMember[];
}

export interface FamilyCreateRequest {
    name: string;
}

export interface FamilyUpdateRequest {
    name: string;
    shoppingDay: DayOfWeek;
    defaultWeekdayServings: MealServings;
    defaultWeekendServings: MealServings;
    noRepeatRecipeDays: number;
    maxWeekdayCookingMinutes: number | null;
    maxWeekendCookingMinutes: number | null;
    isSetupCompleted: boolean;
}
