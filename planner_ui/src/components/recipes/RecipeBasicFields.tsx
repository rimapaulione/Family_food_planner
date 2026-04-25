import {Controller, useFormContext} from 'react-hook-form';
import {useCategories} from '@/hooks/useCategories';
import type {RecipeFormData} from '@/schemas/recipe';
import {FormField} from '@/components/ui/FormField';
import {CATEGORY_LABELS} from '@/constants/categories';
import {inputClass} from '@/utils/inputClass';

export function RecipeBasicFields() {
    const {data: categories} = useCategories();
    const {register, control, formState: {errors}} = useFormContext<RecipeFormData>();

    return (
        <>
            <FormField label="Recipe Name" error={errors.name?.message}>
                <input
                    type="text"
                    {...register('name')}
                    className={inputClass(!!errors.name)}
                    placeholder="e.g. Chicken with Rice"
                />
            </FormField>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
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
        </>
    );
}
