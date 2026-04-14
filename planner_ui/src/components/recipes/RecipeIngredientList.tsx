import type {RecipeIngredient} from '@/types/recipe';

interface RecipeIngredientListProps {
    ingredients: RecipeIngredient[];
}

export function RecipeIngredientList({ingredients}: RecipeIngredientListProps) {
    if (ingredients.length === 0) {
        return <p className="text-sm text-muted-foreground">No ingredients listed.</p>;
    }

    return (
        <ul className="flex flex-col gap-1">
            {ingredients.map((ing) => (
                <li
                    key={ing.id}
                    className="flex items-center justify-between rounded-md border border-border bg-card px-3 py-2 text-sm"
                >
                    <span className="text-foreground">{ing.ingredientName}</span>
                    <span className="text-muted-foreground">
                        {ing.quantity} {ing.unit}
                    </span>
                </li>
            ))}
        </ul>
    );
}
