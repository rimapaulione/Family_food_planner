import {useNavigate} from 'react-router-dom';
import {useUpdateRecipe} from '@/hooks/useRecipes';
import type {RecipeRequest, RecipeResponse} from '@/types/recipe';
import {RecipeForm} from '@/components/recipes/RecipeForm';

interface RecipeEditSectionProps {
    recipe: RecipeResponse;
    recipeId: string;
}

export function RecipeEditSection({recipe, recipeId}: RecipeEditSectionProps) {
    const navigate = useNavigate();
    const updateMutation = useUpdateRecipe();

    const handleSubmit = async (data: RecipeRequest) => {
        await updateMutation.mutateAsync({id: recipeId, data});
        navigate(`/recipes/${recipeId}`);
    };

    return (
        <>
            <h1 className="text-2xl font-bold text-foreground">Edit Recipe</h1>
            <RecipeForm
                initialData={recipe}
                onSubmit={handleSubmit}
                onCancel={() => navigate(`/recipes/${recipeId}`)}
                isPending={updateMutation.isPending}
                submitLabel="Update"
                excludeRecipeId={recipeId}
            />
        </>
    );
}
