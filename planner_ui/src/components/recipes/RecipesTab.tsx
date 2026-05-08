import {useState} from 'react';
import {useDeleteRecipe, useRecipes} from '@/hooks/useRecipes';
import {normalize} from '@/utils/normalize';
import {CATEGORY_LABELS} from '@/constants/categories';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {NotFound} from '@/components/ui/NotFound';
import {PageHeader} from '@/components/ui/PageHeader';
import {RecipeCard} from '@/components/recipes/RecipeCard';

const pillClass = (active: boolean) =>
    `rounded-full px-2 py-0.5 text-[11px] sm:px-2.5 sm:text-xs ${
        active
            ? 'bg-primary text-primary-foreground'
            : 'bg-secondary text-secondary-foreground hover:bg-accent'
    }`;

export function RecipesTab() {
    const [search, setSearch] = useState('');
    const [activeCategoryId, setActiveCategoryId] = useState<number | null>(null);
    const {data: recipes, isLoading, isError, error} = useRecipes();
    const deleteMutation = useDeleteRecipe();

    const visibleCategories = Array.from(
        new Map((recipes ?? []).map((r) => [r.category.id, r.category])).values(),
    ).sort((a, b) => a.name.localeCompare(b.name));

    const filtered = (recipes ?? []).filter((r) => {
        if (search && !normalize(r.name).includes(normalize(search))) return false;
        if (activeCategoryId !== null && r.category.id !== activeCategoryId) return false;
        return true;
    });

    return (
        <div className="flex flex-col gap-4">
            <PageHeader
                title="Recipes"
                addLabel="Add"
                addLabelExtra="Recipe"
                addTo="/recipes/new"
                search={search}
                onSearchChange={setSearch}
                searchPlaceholder="Search recipes..."
            />

            {visibleCategories.length > 1 && (
                <div className="flex flex-wrap gap-1.5">
                    <button
                        type="button"
                        onClick={() => setActiveCategoryId(null)}
                        className={pillClass(activeCategoryId === null)}
                    >
                        All
                    </button>
                    {visibleCategories.map((cat) => (
                        <button
                            key={cat.id}
                            type="button"
                            onClick={() => setActiveCategoryId(cat.id)}
                            className={pillClass(activeCategoryId === cat.id)}
                        >
                            {CATEGORY_LABELS[cat.name] ?? cat.name}
                        </button>
                    ))}
                </div>
            )}

            {isLoading ? (
                <Spinner/>
            ) : isError ? (
                <ErrorMessage error={error} fallback="Failed to load recipes."/>
            ) : !recipes || recipes.length === 0 ? (
                <NotFound message="No recipes yet. Create your first recipe!"/>
            ) : filtered.length === 0 ? (
                <NotFound message="No recipes match your filters."/>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                    {filtered.map((recipe) => (
                        <RecipeCard
                            key={recipe.id}
                            recipe={recipe}
                            onDelete={(id) => deleteMutation.mutate(id)}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
