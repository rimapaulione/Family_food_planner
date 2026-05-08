import {CATEGORY_LABELS} from '@/constants/categories';
import type {Category} from '@/types/recipe';

interface RecipeCategoryBadgeProps {
    category: Category;
}

export function RecipeCategoryBadge({category}: RecipeCategoryBadgeProps) {
    return (
        <span className="rounded-full bg-secondary px-1.5 py-0 text-[10px] text-secondary-foreground sm:px-2 sm:text-[11px]">
            {CATEGORY_LABELS[category.name] || category.name}
        </span>
    );
}
