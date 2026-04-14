import type {Tag} from '@/types/recipe';

interface RecipeTagPickerProps {
    tags: Tag[];
    selectedIds: Set<number>;
    onChange: (ids: Set<number>) => void;
}

export function RecipeTagPicker({tags, selectedIds, onChange}: RecipeTagPickerProps) {
    const toggle = (id: number) => {
        const next = new Set(selectedIds);
        if (next.has(id)) next.delete(id);
        else next.add(id);
        onChange(next);
    };

    return (
        <div className="flex flex-wrap gap-1.5">
            {tags.map((tag) => {
                const selected = selectedIds.has(tag.id);
                return (
                    <button
                        key={tag.id}
                        type="button"
                        onClick={() => toggle(tag.id)}
                        className={`rounded-full px-2.5 py-0.5 text-xs transition ${
                            selected
                                ? 'bg-primary text-primary-foreground'
                                : 'bg-secondary text-secondary-foreground hover:bg-accent'
                        }`}
                    >
                        {tag.name}
                    </button>
                );
            })}
        </div>
    );
}
