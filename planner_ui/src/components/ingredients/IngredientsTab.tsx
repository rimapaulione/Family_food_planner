import {useIngredients} from '@/hooks/useIngredients';
import {Spinner} from '@/components/ui/Spinner';

export function IngredientsTab() {
    const {data: ingredients, isLoading} = useIngredients();

    if (isLoading) return <Spinner/>

    if (!ingredients || ingredients.length === 0) {
        return <p className="text-muted-foreground">No ingredients found.</p>;
    }

    return (
        <div className="grid gap-2">
            {ingredients.map((ingredient) => (
                <div
                    key={ingredient.id}
                    className="flex items-center justify-between rounded-lg border border-border bg-card px-4 py-3"
                >
                    <span className="text-foreground">{ingredient.nameLt}</span>
                    <span className="text-sm text-muted-foreground">{ingredient.unit}</span>
                </div>
            ))}
        </div>
    );
}
