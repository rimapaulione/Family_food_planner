import {Controller, useFormContext} from 'react-hook-form';
import {useTags} from '@/hooks/useTags';
import {useRecipes} from '@/hooks/useRecipes';
import type {RecipeFormData} from '@/schemas/recipe';
import {FormField} from '@/components/ui/FormField';
import {RecipeTagPicker} from '@/components/recipes/RecipeTagPicker';
import {inputClass} from '@/utils/inputClass';

interface RecipeMetaFieldsProps {
    excludeRecipeId?: string;
}

export function RecipeMetaFields({excludeRecipeId}: RecipeMetaFieldsProps) {
    const {data: allTags} = useTags();
    const {data: allRecipes} = useRecipes();
    const {register, control, formState: {errors}} = useFormContext<RecipeFormData>();

    return (
        <>
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
        </>
    );
}
