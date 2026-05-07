import {useState} from 'react';
import {Link} from 'react-router-dom';
import type {RecipeListResponse} from '@/types/recipe';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import {IconStat} from '@/components/ui/IconStat';
import {RecipeCategoryBadge} from '@/components/recipes/RecipeCategoryBadge';
import {RecipeTagList} from '@/components/recipes/RecipeTagList';
import {Users, Clock, Link2, Trash2} from 'lucide-react';
import {FavoriteStar} from '@/components/recipes/FavoriteStar';

interface RecipeCardProps {
    recipe: RecipeListResponse;
    onDelete: (id: string) => void;
}

export function RecipeCard({recipe, onDelete}: RecipeCardProps) {
    const [confirmOpen, setConfirmOpen] = useState(false);

    return (
        <div className="flex flex-col gap-2 rounded-lg border border-border bg-card p-4">
            <div className="flex items-center gap-1.5">
                <Link to={`/recipes/${recipe.id}`} className="font-medium text-foreground hover:underline">
                    {recipe.name}
                </Link>
                <FavoriteStar isFavorite={recipe.isFavorite}/>
                {recipe.hasLeftover && (
                    <span className="group relative inline-flex">
                        <Link2 className="h-3.5 w-3.5 text-muted-foreground"/>
                        <span className="pointer-events-none absolute left-1/2 top-full z-10 mt-1 -translate-x-1/2 whitespace-nowrap rounded-md border border-border bg-card px-2 py-1 text-xs text-card-foreground shadow-md opacity-0 transition-opacity group-hover:opacity-100">
                            Uses leftovers from another recipe
                        </span>
                    </span>
                )}
            </div>

            <div className="mt-1 flex items-center justify-between">
                <RecipeCategoryBadge category={recipe.category}/>
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

            <RecipeTagList tags={recipe.tags}/>

            <ConfirmDialog
                open={confirmOpen}
                onOpenChange={setConfirmOpen}
                title="Delete recipe?"
                description={`"${recipe.name}" will be permanently deleted.`}
                confirmText="Delete"
                destructive
                onConfirm={() => onDelete(recipe.id)}
            />
        </div>
    );
}
