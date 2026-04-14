import {useNavigate, useParams} from 'react-router-dom';
import {useRecipesById, useUpdateRecipe} from '@/hooks/useRecipes';
import type {RecipeRequest} from '@/types/recipe';
import {Spinner} from '@/components/ui/Spinner';
import {BackLink} from '@/components/ui/BackLink';
import {RecipeForm} from '@/components/recipes/RecipeForm';

export function RecipeEditPage() {
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();
    const {data: recipe, isLoading, isError, error} = useRecipesById(id ?? '');
    const updateMutation = useUpdateRecipe();

    const handleSubmit = async (data: RecipeRequest) => {
        if (!id) return;
        await updateMutation.mutateAsync({id, data});
        navigate(`/recipes/${id}`);
    };

    if (isLoading) return <Spinner/>;

    if (isError) {
        return (
            <p className="text-destructive">
                Failed to load recipe. {error instanceof Error ? error.message : ''}
            </p>
        );
    }

    if (!recipe) {
        return (
            <div className="mx-auto max-w-2xl space-y-6">
                <BackLink to="/recipes"/>
                <p className="text-muted-foreground">Recipe not found.</p>
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <BackLink to={`/recipes/${id}`}/>
            <h1 className="text-2xl font-bold text-foreground">Edit Recipe</h1>
            <RecipeForm
                initialData={recipe}
                onSubmit={handleSubmit}
                onCancel={() => navigate(`/recipes/${id}`)}
                isPending={updateMutation.isPending}
                submitLabel="Update"
                excludeRecipeId={id}
            />
        </div>
    );
}
