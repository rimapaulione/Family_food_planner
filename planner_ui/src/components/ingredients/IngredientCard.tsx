import {Pencil, Trash2} from 'lucide-react';
import type {IngredientDetail} from '@/types/ingredient';

interface IngredientCardProps {
    ingredient: IngredientDetail;
    onEdit: () => void;
    onDelete: () => void;
}

export function IngredientCard({ingredient, onEdit, onDelete}: IngredientCardProps) {
    const inUse = ingredient.recipeCount > 0;

    return (
        <div className="flex items-center gap-3 rounded-md border border-border px-3 py-2">
            <span className="flex-1 text-sm font-medium">{ingredient.nameLt}</span>

            {inUse && (
                <span
                    className="rounded-full bg-secondary px-1.5 py-0.5 text-[10px] text-secondary-foreground">
                    {ingredient.recipeCount} recipes
                </span>
            )}
            <span className="text-xs text-muted-foreground">{ingredient.unit}</span>

            <button
                onClick={onEdit}
                className="rounded p-1 text-muted-foreground hover:bg-accent"
            >
                <Pencil className="h-3.5 w-3.5"/>
            </button>
            <button
                onClick={onDelete}
                disabled={inUse}
                title={inUse ? `Used in ${ingredient.recipeCount} recipes` : 'Delete'}
                className={
                    inUse
                        ? 'rounded p-1 cursor-not-allowed text-muted-foreground/30'
                        : 'rounded p-1 text-muted-foreground hover:bg-destructive hover:text-destructive-foreground'
                }
            >
                <Trash2 className="h-3.5 w-3.5"/>
            </button>
        </div>
    );
}
