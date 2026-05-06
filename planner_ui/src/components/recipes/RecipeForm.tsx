import {useEffect} from 'react';
import {Controller, FormProvider, useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {Save} from 'lucide-react';
import {useCategories} from '@/hooks/useCategories';
import {useIngredients, useCreateIngredient} from '@/hooks/useIngredients';
import type {AiRecipeGeneration, RecipeRequest, RecipeResponse} from '@/types/recipe';
import {recipeSchema, type RecipeFormData} from '@/schemas/recipe';
import {Button} from '@/components/ui/Button';
import {RecipeBasicFields} from '@/components/recipes/RecipeBasicFields';
import {RecipeMetaFields} from '@/components/recipes/RecipeMetaFields';
import {RecipeIngredientPicker} from '@/components/recipes/RecipeIngredientPicker';

interface RecipeFormProps {
    initialData?: RecipeResponse;
    aiData?: AiRecipeGeneration | null;
    onSubmit: (data: RecipeRequest) => Promise<void>;
    onCancel: () => void;
    isPending: boolean;
    submitLabel: string;
    excludeRecipeId?: string;
}

export function RecipeForm({
    initialData,
    aiData,
    onSubmit,
    onCancel,
    isPending,
    submitLabel,
    excludeRecipeId,
}: RecipeFormProps) {
    const {data: categories} = useCategories();
    const {data: ingredients} = useIngredients();
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

    const {control, formState: {errors}} = form;

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

    useEffect(() => {
        if (!aiData) return;
        form.reset({
            name: aiData.name,
            categoryId: aiData.categoryId,
            defaultServing: aiData.defaultServing,
            cookingTimeMinutes: aiData.cookingTimeMinutes,
            tagIds: aiData.tagIds,
            leftoverRecipeId: null,
            isFavorite: false,
            notes: aiData.notes ?? '',
            ingredients: [
                ...aiData.ingredients.map((i) => ({
                    ingredientId: i.ingredientId,
                    quantity: i.quantity,
                    unit: i.unit,
                })),
                ...aiData.missingIngredients.map((i) => ({
                    ingredientId: '',
                    quantity: i.quantity,
                    unit: i.unit,
                    isNew: true,
                    name: i.name,
                })),
            ],
        });
    }, [aiData, form]);

    const handleFormSubmit = form.handleSubmit(async (data) => {
        try {
            const resolvedIngredients = await Promise.all(
                (data.ingredients ?? []).map(async (row) => {
                    if (row.isNew && row.name && row.quantity > 0) {
                        const created = await createIngredientMutation.mutateAsync({
                            nameLt: row.name,
                            unit: row.unit,
                        });
                        return {ingredientId: created.id, quantity: row.quantity};
                    }
                    if (row.ingredientId && row.quantity > 0) {
                        return {ingredientId: row.ingredientId, quantity: row.quantity};
                    }
                    return null;
                }),
            );

            await onSubmit({
                name: data.name,
                categoryId: data.categoryId,
                defaultServing: data.defaultServing,
                cookingTimeMinutes: data.cookingTimeMinutes,
                tagIds: data.tagIds?.length ? data.tagIds : undefined,
                leftoverRecipeId: data.leftoverRecipeId || null,
                isFavorite: data.isFavorite,
                notes: data.notes || undefined,
                ingredients: resolvedIngredients.filter((r): r is {ingredientId: string; quantity: number} => r !== null),
            });
        } catch (err: unknown) {
            const message =
                err && typeof err === 'object' && 'response' in err
                    ? (err as {response?: {data?: {message?: string}}}).response?.data?.message
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
        <FormProvider {...form}>
            <form onSubmit={handleFormSubmit} className="space-y-4">
                <RecipeBasicFields/>
                <RecipeMetaFields excludeRecipeId={excludeRecipeId}/>

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
        </FormProvider>
    );
}
