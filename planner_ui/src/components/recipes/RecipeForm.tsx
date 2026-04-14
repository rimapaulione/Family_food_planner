import {useEffect} from 'react';
import {useForm, Controller} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {Save} from 'lucide-react';
import {useCategories} from '@/hooks/useCategories';
import {useTags} from '@/hooks/useTags';
import {useIngredients, useCreateIngredient} from '@/hooks/useIngredients';
import {useRecipes} from '@/hooks/useRecipes';
import type {RecipeRequest, RecipeResponse} from '@/types/recipe';
import {recipeSchema, type RecipeFormData} from '@/schemas/recipe';
import {Button} from '@/components/ui/Button';
import {FormField} from '@/components/ui/FormField';
import {RecipeTagPicker} from '@/components/recipes/RecipeTagPicker';
import {RecipeIngredientPicker} from '@/components/recipes/RecipeIngredientPicker';
import {CATEGORY_LABELS} from '@/constants/categories';
import {inputClass} from '@/utils/inputClass';

interface RecipeFormProps {
    initialData?: RecipeResponse;
    onSubmit: (data: RecipeRequest) => Promise<void>;
    onCancel: () => void;
    isPending: boolean;
    submitLabel: string;
    excludeRecipeId?: string;
}

export function RecipeForm({
    initialData,
    onSubmit,
    onCancel,
    isPending,
    submitLabel,
    excludeRecipeId,
}: RecipeFormProps) {
    const {data: categories} = useCategories();
    const {data: allTags} = useTags();
    const {data: ingredients} = useIngredients();
    const {data: allRecipes} = useRecipes();
    const createIngredientMutation = useCreateIngredient();

    const form = useForm<RecipeFormData>({
        resolver: zodResolver(recipeSchema),
        defaultValues: {
            name: '',
            categoryId: 0,
            defaultServing: 4,
            cookingTimeMinutes: undefined,
            tagIds: [],
            leftoverRecipeId: null,
            isFavorite: false,
            notes: '',
            ingredients: [],
        },
    });

    const {register, control, formState: {errors}} = form;

    useEffect(() => {
        if (initialData) {
            form.reset({
                name: initialData.name,
                categoryId: initialData.category.id,
                defaultServing: initialData.defaultServing,
                cookingTimeMinutes: initialData.cookingTimeMinutes || undefined,
                tagIds: initialData.tags.map((t) => t.id),
                leftoverRecipeId: initialData.leftoverRecipeId || null,
                isFavorite: initialData.isFavorite,
                notes: initialData.notes || '',
                ingredients: initialData.ingredients.map((ing) => ({
                    ingredientId: ing.ingredientId,
                    quantity: ing.quantity,
                    unit: ing.unit,
                })),
            });
        }
    }, [initialData, form]);

    useEffect(() => {
        if (!initialData && categories?.length && form.getValues('categoryId') === 0) {
            form.setValue('categoryId', categories[0].id);
        }
    }, [categories, initialData, form]);

    const handleFormSubmit = form.handleSubmit(async (data) => {
        try {
            await onSubmit({
                name: data.name,
                categoryId: data.categoryId,
                defaultServing: data.defaultServing,
                cookingTimeMinutes: data.cookingTimeMinutes,
                tagIds: data.tagIds?.length ? data.tagIds : undefined,
                leftoverRecipeId: data.leftoverRecipeId || null,
                isFavorite: data.isFavorite,
                notes: data.notes || undefined,
                ingredients: data.ingredients
                    ?.filter((r) => r.ingredientId && r.quantity > 0)
                    .map((r) => ({ingredientId: r.ingredientId, quantity: r.quantity})),
            });
        } catch (err: unknown) {
            const message =
                err && typeof err === 'object' && 'response' in err
                    ? (err as {response?: {data?: {error?: string}}}).response?.data?.error
                    : undefined;
            if (message) {
                form.setError('root', {message});
            }
        }
    });

    const handleCreateIngredient = async (name: string, unit: string) => {
        const created = await createIngredientMutation.mutateAsync({nameLt: name, unit});
        return {id: created.id, unit: created.unit};
    };

    return (
        <form onSubmit={handleFormSubmit} className="space-y-4">
            <FormField label="Recipe Name" error={errors.name?.message}>
                <Controller
                    control={control}
                    name="name"
                    render={({field}) => (
                        <input
                            type="text"
                            value={field.value}
                            onChange={(e) => {
                                const val = e.target.value;
                                field.onChange(val ? val.charAt(0).toUpperCase() + val.slice(1) : '');
                            }}
                            className={inputClass(!!errors.name)}
                            placeholder="e.g. Chicken with Rice"
                        />
                    )}
                />
            </FormField>

            <div className="grid grid-cols-3 gap-4">
                <FormField label="Category" error={errors.categoryId?.message}>
                    <Controller
                        control={control}
                        name="categoryId"
                        render={({field}) => (
                            <select
                                value={field.value}
                                onChange={(e) => field.onChange(Number(e.target.value))}
                                className={inputClass(!!errors.categoryId)}
                            >
                                {categories?.map((c) => (
                                    <option key={c.id} value={c.id}>
                                        {CATEGORY_LABELS[c.name] || c.name}
                                    </option>
                                ))}
                            </select>
                        )}
                    />
                </FormField>

                <FormField label="Servings" error={errors.defaultServing?.message}>
                    <input
                        type="number"
                        min={1}
                        max={50}
                        {...register('defaultServing', {valueAsNumber: true})}
                        className={inputClass(!!errors.defaultServing)}
                    />
                </FormField>

                <FormField label="Cooking time (min)" error={errors.cookingTimeMinutes?.message}>
                    <Controller
                        control={control}
                        name="cookingTimeMinutes"
                        render={({field}) => (
                            <input
                                type="number"
                                min={1}
                                max={500}
                                value={field.value ?? ''}
                                onChange={(e) =>
                                    field.onChange(e.target.value ? Number(e.target.value) : undefined)
                                }
                                placeholder="e.g. 30"
                                className={inputClass(!!errors.cookingTimeMinutes)}
                            />
                        )}
                    />
                </FormField>
            </div>

            <div>
                <label className="mb-1 block text-sm font-medium">Tags</label>
                <Controller
                    control={control}
                    name="tagIds"
                    render={({field}) => (
                        <RecipeTagPicker
                            tags={allTags ?? []}
                            selectedIds={new Set(field.value ?? [])}
                            onChange={(ids) => field.onChange([...ids])}
                        />
                    )}
                />
            </div>

            <FormField label="Uses leftovers from">
                <Controller
                    control={control}
                    name="leftoverRecipeId"
                    render={({field}) => (
                        <select
                            value={field.value ?? ''}
                            onChange={(e) => field.onChange(e.target.value || null)}
                            className={inputClass(false)}
                        >
                            <option value="">— none —</option>
                            {allRecipes
                                ?.filter((r) => r.id !== excludeRecipeId)
                                .map((r) => (
                                    <option key={r.id} value={r.id}>
                                        {r.name}
                                    </option>
                                ))}
                        </select>
                    )}
                />
            </FormField>

            <label className="flex items-center gap-2 text-sm">
                <input
                    type="checkbox"
                    {...register('isFavorite')}
                    className="rounded accent-primary"
                />
                Favorite recipe
            </label>

            <FormField label="Notes" error={errors.notes?.message}>
                <textarea
                    {...register('notes')}
                    rows={2}
                    className={inputClass(!!errors.notes)}
                    placeholder="Optional notes..."
                />
            </FormField>

            <Controller
                control={control}
                name="ingredients"
                render={({field}) => (
                    <RecipeIngredientPicker
                        ingredients={ingredients ?? []}
                        rows={field.value ?? []}
                        onChange={field.onChange}
                        onCreateIngredient={handleCreateIngredient}
                    />
                )}
            />
            <p className="min-h-4 text-xs text-destructive">
                {errors.ingredients ? 'Some ingredients have errors — check quantities' : ''}
            </p>

            {errors.root && (
                <p className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive">
                    {errors.root.message}
                </p>
            )}

            <div className="flex gap-3 pt-4">
                <Button variant="primary" icon={Save} type="submit" disabled={isPending}>
                    {isPending ? 'Saving...' : submitLabel}
                </Button>
                <Button variant="outline" onClick={onCancel}>
                    Cancel
                </Button>
            </div>
        </form>
    );
}
