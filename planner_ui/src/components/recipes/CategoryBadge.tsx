import {CATEGORY_LABELS} from '@/constants/categories';
import type {Category} from '@/types/recipe';

interface CategoryBadgeProps {
    category: Category;
}

export function CategoryBadge({category}: CategoryBadgeProps) {
    return (
        <span className="rounded-full bg-[hsl(var(--secondary))] px-2 py-0.5 text-xs text-[hsl(var(--secondary-foreground))]">
            {CATEGORY_LABELS[category.name] || category.name}
        </span>
    );
}
