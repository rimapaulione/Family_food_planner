import {MealSlotCard} from './MealSlotCard';
import {MEAL_LABELS} from '@/constants/mealPlan';
import type {Family} from '@/types/family';
import type {MealSlot} from '@/types/mealPlan';
import {effectiveServings, isMealActive} from '@/utils/mealPlanHelpers';

interface MealRowProps {
    slot: MealSlot;
    family: Family;
    onSlotClick?: (slotId: string) => void;
    onSlotRemove?: (slotId: string) => void;
    disabled?: boolean;
}

export function MealRow({slot, family, onSlotClick, onSlotRemove, disabled}: MealRowProps) {
    const isMuted = slot.recipeId === null && !isMealActive(slot.date, slot.mealType, family);

    return (
        <div className="flex items-stretch gap-2">
            <div className="w-16 flex-shrink-0 self-center text-xs font-medium text-muted-foreground">
                {MEAL_LABELS[slot.mealType]}
            </div>
            <div className="min-w-0 flex-1">
                <MealSlotCard
                    slot={slot}
                    effectiveServings={effectiveServings(slot, family)}
                    onClick={() => onSlotClick?.(slot.id)}
                    onRemove={onSlotRemove ? () => onSlotRemove(slot.id) : undefined}
                    disabled={disabled}
                    muted={isMuted}
                />
            </div>
        </div>
    );
}
