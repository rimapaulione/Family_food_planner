import {useState, useEffect, useRef} from 'react';
import {UNITS} from '@/constants/units';
import {normalize} from '@/utils/normalize';

interface IngredientOption {
    id: string;
    nameLt: string;
    unit: string;
}

interface IngredientSearchProps {
    ingredients: IngredientOption[];
    selectedId: string;
    onSelect: (id: string, unit: string) => void;
    onCreate: (name: string, unit: string) => Promise<void>;
    autoFocus?: boolean;
}

export function IngredientSearch({
    ingredients,
    selectedId,
    onSelect,
    onCreate,
    autoFocus,
}: IngredientSearchProps) {
    const selected = ingredients.find((i) => i.id === selectedId);
    const [query, setQuery] = useState(selected?.nameLt ?? '');
    const [open, setOpen] = useState(false);
    const [creating, setCreating] = useState(false);
    const [highlightIndex, setHighlightIndex] = useState(-1);
    const ref = useRef<HTMLDivElement>(null);
    const listRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (selected) setQuery(selected.nameLt);
    }, [selected]);

    useEffect(() => {
        const handleClick = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
        };
        document.addEventListener('mousedown', handleClick);
        return () => document.removeEventListener('mousedown', handleClick);
    }, []);

    const sorted = ingredients.slice().sort((a, b) => a.nameLt.localeCompare(b.nameLt, 'lt'));
    const normalizedQuery = normalize(query.trim());
    const filtered = normalizedQuery
        ? sorted.filter((i) => normalize(i.nameLt).includes(normalizedQuery))
        : sorted;
    const visible = filtered;

    const exactMatch = ingredients.find(
        (i) => normalize(i.nameLt) === normalizedQuery,
    );
    const showCreate = query.trim().length > 1 && !exactMatch;

    useEffect(() => {
        setHighlightIndex(-1);
    }, [query]);

    useEffect(() => {
        if (highlightIndex >= 0 && listRef.current) {
            const item = listRef.current.children[highlightIndex] as HTMLElement;
            item?.scrollIntoView({block: 'nearest'});
        }
    }, [highlightIndex]);

    const selectItem = (ing: IngredientOption) => {
        onSelect(ing.id, ing.unit);
        setQuery(ing.nameLt);
        setOpen(false);
        setHighlightIndex(-1);
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (!open) {
            if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
                setOpen(true);
                e.preventDefault();
            }
            return;
        }

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                setHighlightIndex((prev) => Math.min(prev + 1, visible.length - 1));
                break;
            case 'ArrowUp':
                e.preventDefault();
                setHighlightIndex((prev) => Math.max(prev - 1, 0));
                break;
            case 'Enter':
                e.preventDefault();
                if (highlightIndex >= 0 && visible[highlightIndex]) {
                    selectItem(visible[highlightIndex]);
                }
                break;
            case 'Escape':
                setOpen(false);
                setHighlightIndex(-1);
                break;
        }
    };

    const handleCreate = async (unit: string) => {
        setCreating(true);
        const capitalized = query.trim().charAt(0).toUpperCase() + query.trim().slice(1);
        await onCreate(capitalized, unit);
        setOpen(false);
        setCreating(false);
    };

    return (
        <div ref={ref} className="relative flex-1">
            <input
                type="text"
                value={query}
                onChange={(e) => {
                    setQuery(e.target.value);
                    setOpen(true);
                    if (selectedId) onSelect('', 'g');
                }}
                onFocus={() => setOpen(true)}
                onKeyDown={handleKeyDown}
                autoFocus={autoFocus}
                placeholder="Type ingredient name..."
                className="w-full rounded-md border border-input bg-background px-2 py-1.5 text-base md:text-sm outline-none focus:ring-2 focus:ring-ring"
            />
            {open && (
                <div
                    ref={listRef}
                    className="no-scrollbar absolute z-20 mt-1 max-h-48 w-[calc(100vw-2.5rem)] sm:w-full overflow-y-auto overflow-x-hidden rounded-md border border-border bg-background shadow-lg"
                >
                    {visible.map((ing, idx) => (
                        <button
                            key={ing.id}
                            type="button"
                            onClick={() => selectItem(ing)}
                            onMouseEnter={() => setHighlightIndex(idx)}
                            className={`flex w-full items-center justify-between px-3 py-1.5 text-left text-sm hover:bg-accent ${
                                idx === highlightIndex
                                    ? 'bg-accent'
                                    : ing.id === selectedId
                                      ? 'bg-accent/50'
                                      : ''
                            }`}
                        >
                            <span className="truncate">{ing.nameLt}</span>
                            <span className="ml-2 shrink-0 text-xs text-muted-foreground">{ing.unit}</span>
                        </button>
                    ))}
                    {filtered.length === 0 && !showCreate && (
                        <div className="px-3 py-2 text-xs text-muted-foreground">
                            No ingredients found
                        </div>
                    )}
                    {showCreate && (
                        <div className="border-t border-border p-2">
                            <p className="mb-1 text-xs text-muted-foreground">
                                Create &quot;{query.trim()}&quot; as:
                            </p>
                            <div className="flex gap-1">
                                {UNITS.map((unit) => (
                                    <button
                                        key={unit}
                                        type="button"
                                        disabled={creating}
                                        onClick={() => handleCreate(unit)}
                                        className="rounded-md bg-primary px-3 py-1 text-xs text-primary-foreground hover:opacity-90 disabled:opacity-50"
                                    >
                                        {unit}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
