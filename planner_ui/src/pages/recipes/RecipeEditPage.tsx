import {useNavigate, useParams} from 'react-router-dom';
import {useRecipesById, useUpdateRecipe} from '@/hooks/useRecipes';
import type {RecipeRequest} from '@/types/recipe';
import {Spinner} from '@/components/ui/Spinner';
import {BackLink} from '@/components/ui/BackLink';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {NotFound} from '@/components/ui/NotFound';
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

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <BackLink to={`/recipes/${id}`}/>
            {isLoading && <Spinner/>}
            {isError && <ErrorMessage error={error} fallback="Failed to load recipe."/>}
            {!isLoading && !isError && !recipe && <NotFound message="Recipe not found."/>}
            {recipe && (
                <>
                    <h1 className="text-2xl font-bold text-foreground">Edit Recipe</h1>
                    <RecipeForm
                        initialData={recipe}
                        onSubmit={handleSubmit}
                        onCancel={() => navigate(`/recipes/${id}`)}
                        isPending={updateMutation.isPending}
                        submitLabel="Update"
                        excludeRecipeId={id}
                    />
                </>
            )}
        </div>
    );
}
