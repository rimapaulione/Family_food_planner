import type {KeyboardEvent} from 'react';
import {Plus, Clock, Pencil, User, X} from 'lucide-react';
import {Link} from 'react-router-dom';
import {Card} from '@/components/ui/Card';
import {LONG_COOKING_MINUTES} from '@/constants/mealPlan';
import type {MealSlot} from '@/types/mealPlan';
import {isWeekday} from '@/utils/mealPlanHelpers';

interface MealSlotCardProps {
    slot: MealSlot;
    effectiveServings?: number | null;
    onClick?: () => void;
    onRemove?: () => void;
    disabled?: boolean;
    muted?: boolean;
}

export function MealSlotCard({slot, effectiveServings, onClick, onRemove, disabled, muted}: MealSlotCardProps) {
    const isAssigned = slot.recipeId !== null;
    const isLongCook =
        isAssigned
        && slot.cookingTimeMinutes !== null
        && slot.cookingTimeMinutes > LONG_COOKING_MINUTES
        && isWeekday(slot.date);

    const handleClick = () => {
        if (disabled) return;
        onClick?.();
    };

    const handleKeyDown = (e: KeyboardEvent) => {
        if (disabled) return;
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onClick?.();
        }
    };

    const wrapperClass = `group relative block w-full text-left no-underline ${
        disabled ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'
    } ${muted ? 'opacity-40' : ''}`;

    if (!isAssigned) {
        return (
            <div
                role="button"
                tabIndex={disabled ? -1 : 0}
                onClick={handleClick}
                onKeyDown={handleKeyDown}
                className={wrapperClass}
            >
                <Card
                    variant="dashed"
                    padding="sm"
                    className="flex min-h-[3.5rem] items-center justify-center"
                >
                    <Plus className="h-4 w-4 text-muted-foreground"/>
                </Card>
            </div>
        );
    }

    const longCookClass = isLongCook
        ? 'border-orange-300 bg-orange-50 dark:border-orange-700 dark:bg-orange-950/30'
        : '';

    return (
        <Link
            to={`/recipes/${slot.recipeId}`}
            state={{from: '/planner'}}
            className={wrapperClass}
        >
            <Card padding="sm" className={`min-h-[3.5rem] ${longCookClass}`}>
                <div className="flex h-full flex-col justify-between gap-1">
                    <span className="block truncate text-sm font-medium text-foreground">
                        {slot.recipeName}
                    </span>
                    <div className={`flex items-center gap-2 text-xs ${
                        isLongCook ? 'text-orange-700 dark:text-orange-400' : 'text-muted-foreground'
                    }`}>
                        {slot.cookingTimeMinutes !== null && (
                            <span className="inline-flex items-center gap-0.5">
                                <Clock className="h-3 w-3"/>
                                {slot.cookingTimeMinutes}m
                            </span>
                        )}
                        {(effectiveServings ?? slot.servings) !== null && (
                            <span className="inline-flex items-center gap-0.5">
                                <User className="h-3 w-3"/>
                                {effectiveServings ?? slot.servings}
                            </span>
                        )}
                    </div>
                </div>
            </Card>
            {(onClick || onRemove) && !disabled && (
                <div className="absolute -right-2 -top-2 flex gap-1 md:hidden md:group-hover:flex">
                    {onClick && (
                        <button
                            type="button"
                            onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                onClick();
                            }}
                            className="rounded-full bg-secondary p-1.5 text-secondary-foreground shadow"
                            aria-label="Edit slot"
                        >
                            <Pencil className="h-4 w-4"/>
                        </button>
                    )}
                    {onRemove && (
                        <button
                            type="button"
                            onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                onRemove();
                            }}
                            className="rounded-full bg-destructive p-1.5 text-destructive-foreground shadow"
                            aria-label="Remove recipe"
                        >
                            <X className="h-4 w-4"/>
                        </button>
                    )}
                </div>
            )}
        </Link>
    );
}
