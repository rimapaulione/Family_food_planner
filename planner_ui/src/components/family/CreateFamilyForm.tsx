import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {Plus} from 'lucide-react';
import {familyCreateSchema, type FamilyCreateFormData} from '@/schemas/family';
import {Button} from '@/components/ui/Button';
import {FormField} from '@/components/ui/FormField';
import {inputClass} from '@/utils/inputClass';

interface CreateFamilyFormProps {
    onSubmit: (data: FamilyCreateFormData) => Promise<void>;
    isPending: boolean;
}

export function CreateFamilyForm({onSubmit, isPending}: CreateFamilyFormProps) {
    const {register, handleSubmit, formState: {errors}} = useForm<FamilyCreateFormData>({
        resolver: zodResolver(familyCreateSchema),
        defaultValues: {name: ''},
    });

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <FormField label="Family Name" error={errors.name?.message}>
                <input
                    type="text"
                    {...register('name')}
                    className={inputClass(!!errors.name)}
                    placeholder="e.g. Smith Family"
                />
            </FormField>
            <Button
                variant="primary"
                icon={Plus}
                type="submit"
                disabled={isPending}
                className="w-full justify-center"
            >
                {isPending ? 'Creating...' : 'Create Family'}
            </Button>
        </form>
    );
}
