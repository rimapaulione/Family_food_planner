import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Clock, Pencil, Trash2, Users} from 'lucide-react';
import type {RecipeResponse} from '@/types/recipe';
import {useDeleteRecipe} from '@/hooks/useRecipes';
import {IconStat} from '@/components/ui/IconStat';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import {Button} from '@/components/ui/Button';
import {RecipeCategoryBadge} from '@/components/recipes/RecipeCategoryBadge';
import {RecipeTagList} from '@/components/recipes/RecipeTagList';
import {RecipeIngredientList} from '@/components/recipes/RecipeIngredientList';
import {FavoriteStar} from '@/components/recipes/FavoriteStar';

interface RecipeDetailSectionProps {
    recipe: RecipeResponse;
}

export function RecipeDetailSection({recipe}: RecipeDetailSectionProps) {
    const [confirmOpen, setConfirmOpen] = useState(false);
    const navigate = useNavigate();
    const deleteMutation = useDeleteRecipe();

    const handleDelete = () => {
        deleteMutation.mutate(recipe.id, {onSuccess: () => navigate('/recipes')});
    };

    return (
        <>
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
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
                onConfirm={handleDelete}
            />
        </>
    );
}
