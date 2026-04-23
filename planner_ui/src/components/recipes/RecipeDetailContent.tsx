import {useState} from 'react';
import {Clock, Pencil, Trash2, Users} from 'lucide-react';
import type {RecipeResponse} from '@/types/recipe';
import {IconStat} from '@/components/ui/IconStat';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import {Button} from '@/components/ui/Button';
import {RecipeCategoryBadge} from '@/components/recipes/RecipeCategoryBadge';
import {RecipeTagList} from '@/components/recipes/RecipeTagList';
import {RecipeIngredientList} from '@/components/recipes/RecipeIngredientList';
import {FavoriteStar} from '@/components/recipes/FavoriteStar';

interface RecipeDetailContentProps {
    recipe: RecipeResponse;
    onDelete: () => void;
}

export function RecipeDetailContent({recipe, onDelete}: RecipeDetailContentProps) {
    const [confirmOpen, setConfirmOpen] = useState(false);

    return (
        <>
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
                onConfirm={onDelete}
            />
        </>
    );
}
