import {useParams} from 'react-router-dom';
import {useRecipesById} from '@/hooks/useRecipes';
import {Spinner} from '@/components/ui/Spinner';
import {BackLink} from '@/components/ui/BackLink';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {NotFound} from '@/components/ui/NotFound';
import {RecipeEditSection} from '@/components/recipes/RecipeEditSection';

export function RecipeEditPage() {
    const {id} = useParams<{id: string}>();
    const {data: recipe, isLoading, isError, error} = useRecipesById(id ?? '');

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <BackLink to={`/recipes/${id}`}/>
            {isLoading && <Spinner/>}
            {isError && <ErrorMessage error={error} fallback="Failed to load recipe."/>}
            {!isLoading && !isError && !recipe && <NotFound message="Recipe not found."/>}
            {recipe && id && <RecipeEditSection recipe={recipe} recipeId={id}/>}
        </div>
    );
}
