import {CATEGORY_LABELS} from '@/constants/categories';
import type {Category} from '@/types/recipe';

interface RecipeCategoryBadgeProps {
    category: Category;
}

export function RecipeCategoryBadge({category}: RecipeCategoryBadgeProps) {
    return (
        <span className="rounded-full bg-secondary px-2 py-0.5 text-xs text-secondary-foreground">
            {CATEGORY_LABELS[category.name] || category.name}
        </span>
    );
}
