import {Fragment} from 'react';
import {MealSlotCard} from './MealSlotCard';
import type {Family} from '@/types/family';
import type {MealPlan, MealType} from '@/types/mealPlan';
import {MEAL_TYPE} from '@/types/mealPlan';

const MEAL_ORDER: MealType[] = [MEAL_TYPE.BREAKFAST, MEAL_TYPE.LUNCH, MEAL_TYPE.DINNER];

const MEAL_LABELS: Record<MealType, string> = {
    BREAKFAST: 'Breakfast',
    LUNCH: 'Lunch',
    DINNER: 'Dinner',
};

interface MealPlanGridProps {
    plan: MealPlan;
    family: Family;
    onSlotClick?: (slotId: string) => void;
    disabled?: boolean;
}

export function MealPlanGrid({plan, family, onSlotClick, disabled}: MealPlanGridProps) {
    const days = getWeekDates(plan.startDate);

    return (
        <div className="grid gap-2 grid-cols-[80px_repeat(7,minmax(0,1fr))]">
            <div/>
            {days.map((date) => (
                <div key={date} className="text-center text-xs font-semibold text-muted-foreground">
                    {formatDayHeader(date)}
                </div>
            ))}

            {MEAL_ORDER.map((mealType) => (
                <Fragment key={mealType}>
                    <div className="self-center text-xs font-medium text-muted-foreground">
                        {MEAL_LABELS[mealType]}
                    </div>
                    {days.map((date) => {
                        const slot = plan.slots.find(
                            (s) => s.date === date && s.mealType === mealType,
                        );
                        if (!slot) return <div key={`${mealType}-${date}`}/>;
                        const isMuted = slot.recipeId === null && !isMealActive(date, mealType, family);
                        return (
                            <div key={`${mealType}-${date}`}>
                                <MealSlotCard
                                    slot={slot}
                                    onClick={() => onSlotClick?.(slot.id)}
                                    disabled={disabled}
                                    muted={isMuted}
                                />
                            </div>
                        );
                    })}
                </Fragment>
            ))}
        </div>
    );
}

function isMealActive(date: string, mealType: MealType, family: Family): boolean {
    const dow = new Date(date + 'T00:00:00Z').getUTCDay();
    const isWeekend = dow === 0 || dow === 6;
    const servings = isWeekend ? family.defaultWeekendServings : family.defaultWeekdayServings;
    if (mealType === MEAL_TYPE.BREAKFAST) return servings.breakfast !== null;
    if (mealType === MEAL_TYPE.LUNCH) return servings.lunch !== null;
    if (mealType === MEAL_TYPE.DINNER) return servings.dinner !== null;
    return false;
}

function getWeekDates(startDate: string): string[] {
    const dates: string[] = [];
    const start = new Date(startDate + 'T00:00:00Z');
    for (let i = 0; i < 7; i++) {
        const d = new Date(start);
        d.setUTCDate(start.getUTCDate() + i);
        dates.push(d.toISOString().slice(0, 10));
    }
    return dates;
}

function formatDayHeader(iso: string): string {
    const d = new Date(iso + 'T00:00:00Z');
    const weekday = d.toLocaleDateString('en-US', {weekday: 'short', timeZone: 'UTC'});
    const day = d.getUTCDate();
    return `${weekday} ${day}`;
}
