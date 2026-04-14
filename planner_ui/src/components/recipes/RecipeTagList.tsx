import type {Tag} from '@/types/recipe';

interface TagListProps {
    tags: Tag[];
}

export function RecipeTagList({tags}: TagListProps) {
    if (tags.length === 0) return null;
    return (
        <div className="flex flex-wrap items-center gap-1">
            {tags.map((tag) => (
                <span
                    key={tag.id}
                    className="rounded-full bg-secondary px-1.5 py-0.5 text-[10px] text-secondary-foreground"
                >
                    {tag.name}
                </span>
            ))}
        </div>
    );
}
