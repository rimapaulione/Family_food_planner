import {useNavigate, useParams} from 'react-router-dom';
import {useDeleteRecipe, useRecipesById} from '@/hooks/useRecipes';
import {Spinner} from '@/components/ui/Spinner';
import {BackLink} from '@/components/ui/BackLink';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {NotFound} from '@/components/ui/NotFound';
import {RecipeDetailContent} from '@/components/recipes/RecipeDetailContent';

export function RecipeDetailPage() {
    const {id} = useParams<{id: string}>();
    const navigate = useNavigate();
    const {data: recipe, isLoading, isError, error} = useRecipesById(id ?? '');
    const deleteMutation = useDeleteRecipe();

    const handleDelete = () => {
        if (!recipe) return;
        deleteMutation.mutate(recipe.id, {onSuccess: () => navigate('/recipes')});
    };

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <BackLink to="/recipes"/>
            {isLoading && <Spinner/>}
            {isError && <ErrorMessage error={error} fallback="Failed to load recipe."/>}
            {!isLoading && !isError && !recipe && <NotFound message="Recipe not found."/>}
            {recipe && <RecipeDetailContent recipe={recipe} onDelete={handleDelete}/>}
        </div>
    );
}
