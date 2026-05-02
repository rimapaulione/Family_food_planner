import {Plus, Clock, User} from 'lucide-react';
import {Card} from '@/components/ui/Card';
import type {MealSlot} from '@/types/mealPlan';

interface MealSlotCardProps {
    slot: MealSlot;
    onClick?: () => void;
    disabled?: boolean;
}

export function MealSlotCard({slot, onClick, disabled}: MealSlotCardProps) {
    const isAssigned = slot.recipeId !== null;

    const handleClick = () => {
        if (disabled) return;
        onClick?.();
    };

    return (
        <button
            type="button"
            onClick={handleClick}
            disabled={disabled}
            className="w-full text-left disabled:cursor-not-allowed disabled:opacity-50"
        >
            <Card variant={isAssigned ? 'solid' : 'dashed'} padding="sm">
                {isAssigned ? (
                    <div className="space-y-1">
                        <p className="text-sm font-medium text-foreground">{slot.recipeName}</p>
                        <div className="flex items-center gap-2 text-xs text-muted-foreground">
                            {slot.cookingTimeMinutes !== null && (
                                <span className="inline-flex items-center gap-0.5">
                                    <Clock className="h-3 w-3"/>
                                    {slot.cookingTimeMinutes}m
                                </span>
                            )}
                            {slot.servings !== null && (
                                <span className="inline-flex items-center gap-0.5">
                                    <User className="h-3 w-3"/>
                                    {slot.servings}
                                </span>
                            )}
                        </div>
                    </div>
                ) : (
                    <div className="flex items-center justify-center text-muted-foreground">
                        <Plus className="h-4 w-4"/>
                    </div>
                )}
            </Card>
        </button>
    );
}
