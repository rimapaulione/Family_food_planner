import {Fragment} from 'react';
import {Clock} from 'lucide-react';
import {MealSlotCard} from './MealSlotCard';
import type {Family} from '@/types/family';
import type {MealPlan, MealType} from '@/types/mealPlan';
import {MEAL_TYPE} from '@/types/mealPlan';
import {
    effectiveServings,
    formatDayOfMonth,
    formatWeekdayShort,
    getWeekDates,
    isMealActive,
    isWeekday,
    totalCookingTimeForDay,
} from '@/utils/mealPlanHelpers';

const LONG_DAILY_COOK_MINUTES = 60;

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
    onSlotRemove?: (slotId: string) => void;
    disabled?: boolean;
}

export function MealPlanGrid({plan, family, onSlotClick, onSlotRemove, disabled}: MealPlanGridProps) {
    const days = getWeekDates(plan.startDate);

    return (
        <div className="grid gap-2 grid-cols-[80px_repeat(7,minmax(0,1fr))]">
            <div/>
            {days.map((date) => {
                const total = totalCookingTimeForDay(date, plan);
                const isWknd = !isWeekday(date);
                const totalIsLong = isWeekday(date) && total > LONG_DAILY_COOK_MINUTES;
                return (
                    <div
                        key={date}
                        className={`rounded text-center ${
                            isWknd ? 'bg-blue-50 dark:bg-blue-950/30' : ''
                        }`}
                    >
                        <div className="text-xs font-semibold text-foreground">
                            {formatWeekdayShort(date)}
                        </div>
                        <div className="text-xs text-muted-foreground">
                            {formatDayOfMonth(date)}
                        </div>
                        {total > 0 && (
                            <div
                                className={`mt-0.5 inline-flex items-center gap-0.5 text-xs ${
                                    totalIsLong ? 'font-medium text-orange-500' : 'text-muted-foreground'
                                }`}
                            >
                                <Clock className="h-3 w-3"/>
                                {total}m
                            </div>
                        )}
                    </div>
                );
            })}

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
                                    effectiveServings={effectiveServings(slot, family)}
                                    onClick={() => onSlotClick?.(slot.id)}
                                    onRemove={onSlotRemove ? () => onSlotRemove(slot.id) : undefined}
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
