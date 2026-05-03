import {useLocation, useParams} from 'react-router-dom';
import {useRecipesById} from '@/hooks/useRecipes';
import {Spinner} from '@/components/ui/Spinner';
import {BackLink} from '@/components/ui/BackLink';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {NotFound} from '@/components/ui/NotFound';
import {RecipeDetailSection} from '@/components/recipes/RecipeDetailSection';

export function RecipeDetailPage() {
    const {id} = useParams<{id: string}>();
    const location = useLocation();
    const backTo = (location.state as {from?: string} | null)?.from ?? '/recipes';
    const {data: recipe, isLoading, isError, error} = useRecipesById(id ?? '');

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <BackLink to={backTo}/>
            {isLoading && <Spinner/>}
            {isError && <ErrorMessage error={error} fallback="Failed to load recipe."/>}
            {!isLoading && !isError && !recipe && <NotFound message="Recipe not found."/>}
            {recipe && <RecipeDetailSection recipe={recipe}/>}
        </div>
    );
}
