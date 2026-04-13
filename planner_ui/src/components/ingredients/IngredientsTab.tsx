import {useState} from 'react';
import {
    useCreateIngredient,
    useDeleteIngredient,
    useIngredientsWithRecipeCount,
    useUpdateIngredient,
} from '@/hooks/useIngredients';
import type {IngredientDetail} from '@/types/ingredient';
import {Spinner} from '@/components/ui/Spinner';
import {PageHeader} from '@/components/ui/PageHeader';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import {IngredientCard} from '@/components/ingredients/IngredientCard';
import {IngredientForm} from '@/components/ingredients/IngredientForm';

export function IngredientsTab() {
    const [search, setSearch] = useState('');
    const {data: ingredients, isLoading, isError, error} = useIngredientsWithRecipeCount();

    const createMutation = useCreateIngredient();
    const updateMutation = useUpdateIngredient();
    const deleteMutation = useDeleteIngredient();

    const [addIngredient, setAddIngredient] = useState(false);
    const [editingId, setEditingId] = useState<string | null>(null);
    const [toDelete, setToDelete] = useState<IngredientDetail | null>(null);

    const filtered = (ingredients ?? [])
        .filter((i) => !search || i.nameLt.toLowerCase().includes(search.toLowerCase()))
        .sort((a, b) => a.nameLt.localeCompare(b.nameLt, 'lt'));

    const handleCreate = async (data: {nameLt: string; unit: string}) => {
        await createMutation.mutateAsync(data);
        setAddIngredient(false);
    };

    const handleUpdate = async (id: string, data: {nameLt: string; unit: string}) => {
        await updateMutation.mutateAsync({id, data});
        setEditingId(null);
    };

    return (
        <div className="flex flex-col gap-4">
            <PageHeader
                title="Ingredients"
                addLabel="Add Ingredient"
                onAddClick={() => setAddIngredient(true)}
                search={search}
                onSearchChange={setSearch}
                searchPlaceholder="Search ingredients..."
            />

            {addIngredient && (
                <IngredientForm
                    onSave={handleCreate}
                    onCancel={() => setAddIngredient(false)}
                />
            )}

            {isLoading ? (
                <Spinner/>
            ) : isError ? (
                <p className="text-destructive">
                    Failed to load ingredients. {error instanceof Error ? error.message : ''}
                </p>
            ) : !ingredients || ingredients.length === 0 ? (
                <p className="text-muted-foreground">No ingredients yet.</p>
            ) : filtered.length === 0 ? (
                <p className="text-muted-foreground">No ingredients match your search.</p>
            ) : (
                <div className="space-y-1">
                    {filtered.map((ing) =>
                        editingId === ing.id ? (
                            <IngredientForm
                                key={ing.id}
                                initialName={ing.nameLt}
                                initialUnit={ing.unit}
                                onSave={(data) => handleUpdate(ing.id, data)}
                                onCancel={() => setEditingId(null)}
                            />
                        ) : (
                            <IngredientCard
                                key={ing.id}
                                ingredient={ing}
                                onEdit={() => setEditingId(ing.id)}
                                onDelete={() => setToDelete(ing)}
                            />
                        ),
                    )}
                </div>
            )}

            <ConfirmDialog
                open={toDelete !== null}
                onOpenChange={(open) => { if (!open) setToDelete(null); }}
                title="Delete ingredient?"
                description={toDelete ? `"${toDelete.nameLt}" will be permanently deleted.` : ''}
                confirmText="Delete"
                destructive
                onConfirm={() => {
                    if (toDelete) deleteMutation.mutate(toDelete.id);
                    setToDelete(null);
                }}
            />
        </div>
    );
}
