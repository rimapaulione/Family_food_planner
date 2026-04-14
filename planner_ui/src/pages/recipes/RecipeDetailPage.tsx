import {useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {Pencil, Trash2} from 'lucide-react';
import {useDeleteRecipe, useRecipesById} from '@/hooks/useRecipes';
import {Spinner} from '@/components/ui/Spinner';
import {BackLink} from '@/components/ui/BackLink';
import {IconStat} from '@/components/ui/IconStat';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import {Button} from '@/components/ui/Button';
import {RecipeCategoryBadge} from '@/components/recipes/RecipeCategoryBadge';
import {RecipeTagList} from '@/components/recipes/RecipeTagList';
import {RecipeIngredientList} from '@/components/recipes/RecipeIngredientList';
import {FavoriteStar} from '@/components/recipes/FavoriteStar';
import {Clock, Users} from 'lucide-react';

export function RecipeDetailPage() {
    const {id} = useParams<{ id: string }>();
    const navigate = useNavigate();
    const {data: recipe, isLoading, isError, error} = useRecipesById(id ?? '');
    const deleteMutation = useDeleteRecipe();
    const [confirmOpen, setConfirmOpen] = useState(false);

    const handleDelete = async () => {
        deleteMutation.mutate(recipe.id, {
            onSuccess: () => navigate('/recipes'),
        });
    }

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
            <div className="flex flex-col gap-4">
                <BackLink to="/recipes"/>
                <p className="text-muted-foreground">Recipe not found.</p>
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <BackLink to="/recipes"/>

            <div className="flex items-start justify-between gap-4">
                <div className="flex flex-col gap-2">
                    <h1 className="flex items-center gap-2 text-2xl font-bold text-foreground">
                        {recipe.name}
                        <FavoriteStar isFavorite={recipe.isFavorite} className="h-5 w-5"/>
                    </h1>
                    <div className="flex items-center gap-3 text-sm text-muted-foreground">
                        <RecipeCategoryBadge category={recipe.category}/>
                        <IconStat icon={Users}>{recipe.defaultServing}</IconStat>
                        {recipe.cookingTimeMinutes && (
                            <IconStat icon={Clock}>{recipe.cookingTimeMinutes}min</IconStat>
                        )}
                    </div>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" icon={Pencil} to={`/recipes/${recipe.id}/edit`}>Edit</Button>
                    <Button variant="destructive" icon={Trash2} onClick={() => setConfirmOpen(true)}>Delete</Button>
                </div>
            </div>

            <RecipeTagList tags={recipe.tags}/>

            <section className="flex flex-col gap-2">
                <h2 className="text-lg font-semibold text-foreground">Ingredients</h2>
                <RecipeIngredientList ingredients={recipe.ingredients}/>
            </section>

            {recipe.notes && (
                <section className="flex flex-col gap-2">
                    <h2 className="text-lg font-semibold text-foreground">Notes</h2>
                    <p className="whitespace-pre-wrap text-sm text-muted-foreground">{recipe.notes}</p>
                </section>
            )}

            <ConfirmDialog
                open={confirmOpen}
                onOpenChange={setConfirmOpen}
                title="Delete recipe?"
                description={`"${recipe.name}" will be permanently deleted.`}
                confirmText="Delete"
                destructive
                onConfirm={() => {
                    handleDelete()
                }}
            />
        </div>
    );
}
