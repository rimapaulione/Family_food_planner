import {useState} from 'react';
import {Link} from 'react-router-dom';
import type {RecipeListResponse} from '@/types/recipe';
import {useDeleteRecipe} from '@/hooks/useRecipes';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import {IconStat} from '@/components/ui/IconStat';
import {CategoryBadge} from '@/components/recipes/CategoryBadge';
import {TagList} from '@/components/recipes/TagList';
import {Users, Clock, Trash2} from 'lucide-react';

interface RecipeCardProps {
    recipe: RecipeListResponse;
}

export function RecipeCard({recipe}: RecipeCardProps) {
    const deleteMutation = useDeleteRecipe();
    const [confirmOpen, setConfirmOpen] = useState(false);

    return (
        <div className="flex flex-col gap-2 rounded-lg border border-border bg-card p-4">
            <Link to={`/recipes/${recipe.id}`} className="font-medium text-foreground hover:underline">
                {recipe.name}
            </Link>

            <div className="mt-1 flex items-center justify-between">
                <CategoryBadge category={recipe.category}/>
                <div className="flex items-center gap-3 text-xs text-muted-foreground">
                    <span>{recipe.ingredientCount} ingr.</span>
                    <IconStat icon={Users}>{recipe.defaultServing}</IconStat>
                    {recipe.cookingTimeMinutes && (
                        <IconStat icon={Clock}>{recipe.cookingTimeMinutes}min</IconStat>
                    )}
                    <button
                        onClick={() => setConfirmOpen(true)}
                        className="rounded p-1 text-muted-foreground hover:bg-destructive hover:text-destructive-foreground"
                    >
                        <Trash2 className="h-3 w-3"/>
                    </button>
                </div>
            </div>

            <TagList tags={recipe.tags}/>

            <ConfirmDialog
                open={confirmOpen}
                onOpenChange={setConfirmOpen}
                title="Delete recipe?"
                description={`"${recipe.name}" will be permanently deleted.`}
                confirmText="Delete"
                destructive
                onConfirm={() => deleteMutation.mutate(recipe.id)}
            />
        </div>
    );
}
