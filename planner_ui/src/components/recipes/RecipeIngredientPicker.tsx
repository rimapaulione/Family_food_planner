import {Plus, Trash2} from 'lucide-react';
import {IngredientSearch} from '@/components/recipes/IngredientSearch';
import type {Ingredient} from '@/types/ingredient';

export interface RecipeIngredientRow {
    ingredientId: string;
    quantity: number;
    unit: string;
    isNew?: boolean;
    name?: string;
}

interface RecipeIngredientPickerProps {
    ingredients: Ingredient[];
    rows: RecipeIngredientRow[];
    onChange: (rows: RecipeIngredientRow[]) => void;
    onCreateIngredient: (name: string, unit: string) => Promise<{id: string; unit: string}>;
}

export function RecipeIngredientPicker({
    ingredients,
    rows,
    onChange,
    onCreateIngredient,
}: RecipeIngredientPickerProps) {
    const addRow = () => {
        onChange([...rows, {ingredientId: '', quantity: 0, unit: 'g'}]);
    };

    const removeRow = (index: number) => {
        onChange(rows.filter((_, i) => i !== index));
    };

    const updateRow = (index: number, updates: Partial<RecipeIngredientRow>) => {
        const updated = [...rows];
        updated[index] = {...updated[index], ...updates};
        onChange(updated);
    };

    const focusQuantity = (index: number) => {
        if (!window.matchMedia('(pointer: fine)').matches) return;
        setTimeout(() => document.getElementById(`qty-${index}`)?.focus(), 0);
    };

    return (
        <div className="space-y-2">
            <label className="text-sm font-medium">Ingredients</label>

            {rows.map((row, idx) => (
                <div key={idx} className="flex items-center gap-2">
                    {row.isNew ? (
                        <div className="relative flex-1">
                            <input
                                type="text"
                                value={row.name ?? ''}
                                onChange={(e) => updateRow(idx, {name: e.target.value})}
                                placeholder="New ingredient name"
                                className="w-full rounded-md border border-input bg-background px-2 py-1.5 text-base md:text-sm outline-none focus:ring-2 focus:ring-ring"
                            />
                        </div>
                    ) : (
                        <IngredientSearch
                            ingredients={ingredients}
                            selectedId={row.ingredientId}
                            onSelect={(id, unit) => {
                                updateRow(idx, {ingredientId: id, unit});
                                focusQuantity(idx);
                            }}
                            onCreate={async (name, unit) => {
                                const created = await onCreateIngredient(name, unit);
                                updateRow(idx, {ingredientId: created.id, unit: created.unit});
                                focusQuantity(idx);
                            }}
                            autoFocus={!row.ingredientId}
                        />
                    )}
                    <input
                        id={`qty-${idx}`}
                        type="number"
                        min={0}
                        step="any"
                        value={row.quantity || ''}
                        onChange={(e) => updateRow(idx, {quantity: Number(e.target.value)})}
                        onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addRow(); } }}
                        placeholder="Qty"
                        className="w-20 rounded-md border border-input bg-background px-2 py-1.5 text-base md:text-sm outline-none focus:ring-2 focus:ring-ring"
                    />
                    <span className="w-10 text-center text-sm text-muted-foreground">
                        {row.unit || '—'}
                    </span>
                    <button
                        type="button"
                        onClick={() => removeRow(idx)}
                        className="rounded p-1 text-muted-foreground hover:text-destructive"
                    >
                        <Trash2 className="h-4 w-4"/>
                    </button>
                </div>
            ))}

            <button
                type="button"
                onClick={addRow}
                className="flex items-center gap-1 rounded-md bg-secondary px-2 py-1 text-xs"
            >
                <Plus className="h-3 w-3"/> Add ingredient
            </button>
        </div>
    );
}
