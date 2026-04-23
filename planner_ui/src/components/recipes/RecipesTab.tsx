import {useState} from 'react';
import {useRecipes} from '@/hooks/useRecipes';
import {normalize} from '@/utils/normalize';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {NotFound} from '@/components/ui/NotFound';
import {PageHeader} from '@/components/ui/PageHeader';
import {RecipeCard} from '@/components/recipes/RecipeCard';

export function RecipesTab() {
    const [search, setSearch] = useState('');
    const {data: recipes, isLoading, isError, error} = useRecipes();

    const filtered = (recipes ?? []).filter(
        (r) => !search || normalize(r.name).includes(normalize(search)),
    );

    return (
        <div className="flex flex-col gap-4">
            <PageHeader
                title="Recipes"
                addLabel="Add Recipe"
                addTo="/recipes/new"
                search={search}
                onSearchChange={setSearch}
                searchPlaceholder="Search recipes..."
            />

            {isLoading ? (
                <Spinner/>
            ) : isError ? (
                <ErrorMessage error={error} fallback="Failed to load recipes."/>
            ) : !recipes || recipes.length === 0 ? (
                <NotFound message="No recipes yet. Create your first recipe!"/>
            ) : filtered.length === 0 ? (
                <NotFound message="No recipes match your search."/>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                    {filtered.map((recipe) => (
                        <RecipeCard key={recipe.id} recipe={recipe}/>
                    ))}
                </div>
            )}
        </div>
    );
}
