import {Controller, useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {Save} from 'lucide-react';
import {familyFormSchema, type FamilyFormData} from '@/schemas/family';
import {Button} from '@/components/ui/Button';
import {FormField} from '@/components/ui/FormField';
import {ShoppingDaySelect} from '@/components/family/ShoppingDaySelect';
import {MealServingsInput} from '@/components/family/MealServingsInput';
import {inputClass} from '@/utils/inputClass';

interface FamilyFormProps {
    initialData: FamilyFormData;
    onSubmit: (data: FamilyFormData) => Promise<void>;
    onCancel?: () => void;
    submitLabel: string;
    isPending: boolean;
}

export function FamilyForm({
    initialData,
    onSubmit,
    onCancel,
    submitLabel,
    isPending,
}: FamilyFormProps) {
    const {register, handleSubmit, control, formState: {errors}} = useForm<FamilyFormData>({
        resolver: zodResolver(familyFormSchema),
        defaultValues: initialData,
    });

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <FormField label="Family Name" error={errors.name?.message}>
                <input
                    type="text"
                    {...register('name')}
                    className={inputClass(!!errors.name)}
                />
            </FormField>

            <FormField label="Shopping Day">
                <Controller
                    control={control}
                    name="shoppingDay"
                    render={({field}) => (
                        <ShoppingDaySelect value={field.value} onChange={field.onChange}/>
                    )}
                />
            </FormField>

            <Controller
                control={control}
                name="defaultWeekdayServings"
                render={({field}) => (
                    <MealServingsInput
                        label="Weekday Meals"
                        value={field.value}
                        onChange={field.onChange}
                    />
                )}
            />

            <Controller
                control={control}
                name="defaultWeekendServings"
                render={({field}) => (
                    <MealServingsInput
                        label="Weekend Meals"
                        value={field.value}
                        onChange={field.onChange}
                    />
                )}
            />

            <FormField
                label="Don't repeat recipes within (days)"
                error={errors.noRepeatRecipeDays?.message}
            >
                <input
                    type="number"
                    min={0}
                    max={90}
                    {...register('noRepeatRecipeDays', {valueAsNumber: true})}
                    className={inputClass(!!errors.noRepeatRecipeDays)}
                />
            </FormField>

            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <FormField
                    label="Total weekday cook time (min)"
                    error={errors.maxWeekdayCookingMinutes?.message}
                >
                    <input
                        type="number"
                        min={1}
                        max={600}
                        placeholder="No limit"
                        {...register('maxWeekdayCookingMinutes', {
                            setValueAs: (v) => (v === '' || v === null ? null : Number(v)),
                        })}
                        className={inputClass(!!errors.maxWeekdayCookingMinutes)}
                    />
                </FormField>
                <FormField
                    label="Total weekend cook time (min)"
                    error={errors.maxWeekendCookingMinutes?.message}
                >
                    <input
                        type="number"
                        min={1}
                        max={600}
                        placeholder="No limit"
                        {...register('maxWeekendCookingMinutes', {
                            setValueAs: (v) => (v === '' || v === null ? null : Number(v)),
                        })}
                        className={inputClass(!!errors.maxWeekendCookingMinutes)}
                    />
                </FormField>
            </div>

            <div className="flex gap-3 pt-2">
                <Button
                    variant="primary"
                    icon={Save}
                    type="submit"
                    disabled={isPending}
                    className="flex-1 justify-center"
                >
                    {isPending ? 'Saving...' : submitLabel}
                </Button>
                {onCancel && (
                    <Button variant="outline" onClick={onCancel}>
                        Cancel
                    </Button>
                )}
            </div>
        </form>
    );
}
