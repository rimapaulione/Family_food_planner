import {useState} from 'react';
import {useRecipes} from '@/hooks/useRecipes';
import {Spinner} from '@/components/ui/Spinner';
import {PageHeader} from '@/components/ui/PageHeader';
import {RecipeCard} from '@/components/recipes/RecipeCard';

export function RecipesTab() {
    const [search, setSearch] = useState('');
    const {data: recipes, isLoading} = useRecipes(search);

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
            ) : !recipes || recipes.length === 0 ? (
                <p className="text-muted-foreground">No recipes yet. Create your first recipe!</p>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                    {recipes.map((recipe) => (
                        <RecipeCard key={recipe.id} recipe={recipe}/>
                    ))}
                </div>
            )}
        </div>
    );
}
