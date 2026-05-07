import {useState} from 'react';
import {toast} from 'sonner';
import {X} from 'lucide-react';
import {normalize} from '@/utils/normalize';
import {
    useCreateIngredient,
    useDeleteIngredient,
    useIngredientsWithRecipeCount,
    useUpdateIngredient,
} from '@/hooks/useIngredients';
import type {IngredientDetail} from '@/types/ingredient';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {NotFound} from '@/components/ui/NotFound';
import {PageHeader} from '@/components/ui/PageHeader';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import {IngredientCard} from '@/components/ingredients/IngredientCard';
import {IngredientForm} from '@/components/ingredients/IngredientForm';
import {IngredientSearch} from '@/components/recipes/IngredientSearch';

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
        .filter((i) => !search || normalize(i.nameLt).includes(normalize(search)))
        .sort((a, b) => a.nameLt.localeCompare(b.nameLt, 'lt'));

    const handleUpdate = async (id: string, data: {nameLt: string; unit: string}) => {
        await updateMutation.mutateAsync({id, data});
        setEditingId(null);
    };

    return (
        <div className="flex flex-col gap-4">
            <PageHeader
                title="Ingredients"
                addLabel="Add"
                addLabelExtra="Ingredient"
                onAddClick={() => setAddIngredient(true)}
                search={search}
                onSearchChange={setSearch}
                searchPlaceholder="Search ingredients..."
            />

            {addIngredient && (
                <div className="flex items-center gap-2 rounded-md border border-dashed border-primary px-3 py-2">
                    <IngredientSearch
                        ingredients={ingredients ?? []}
                        selectedId=""
                        onSelect={() => {
                            toast.info('Already in your library');
                            setAddIngredient(false);
                        }}
                        onCreate={async (name, unit) => {
                            await createMutation.mutateAsync({nameLt: name, unit});
                            setAddIngredient(false);
                        }}
                        autoFocus
                    />
                    <button
                        onClick={() => setAddIngredient(false)}
                        aria-label="Cancel"
                        className="rounded p-1 text-muted-foreground hover:bg-accent"
                    >
                        <X className="h-4 w-4"/>
                    </button>
                </div>
            )}

            {isLoading ? (
                <Spinner/>
            ) : isError ? (
                <ErrorMessage error={error} fallback="Failed to load ingredients."/>
            ) : !ingredients || ingredients.length === 0 ? (
                <NotFound message="No ingredients yet."/>
            ) : filtered.length === 0 ? (
                <NotFound message="No ingredients match your search."/>
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
