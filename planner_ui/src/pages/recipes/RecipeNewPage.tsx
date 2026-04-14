import {useNavigate} from 'react-router-dom';
import {useCreateRecipe} from '@/hooks/useRecipes';
import type {RecipeRequest} from '@/types/recipe';
import {BackLink} from '@/components/ui/BackLink';
import {RecipeForm} from '@/components/recipes/RecipeForm';

export function RecipeNewPage() {
    const navigate = useNavigate();
    const createMutation = useCreateRecipe();

    const handleSubmit = async (data: RecipeRequest) => {
        await createMutation.mutateAsync(data);
        navigate('/recipes');
    };

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <BackLink to="/recipes"/>
            <h1 className="text-2xl font-bold text-foreground">New Recipe</h1>
            <RecipeForm
                onSubmit={handleSubmit}
                onCancel={() => navigate('/recipes')}
                isPending={createMutation.isPending}
                submitLabel="Create"
            />
        </div>
    );
}
