import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {Save} from 'lucide-react';
import {profileUpdateSchema, type ProfileUpdateFormData} from '@/schemas/user';
import {Button} from '@/components/ui/Button';
import {FormField} from '@/components/ui/FormField';
import {inputClass} from '@/utils/inputClass';

interface ProfileFormProps {
    initialDisplayName: string;
    initialAvatarUrl: string | null;
    onSubmit: (data: ProfileUpdateFormData) => Promise<void>;
    onCancel: () => void;
    isPending: boolean;
}

export function ProfileForm({
    initialDisplayName,
    initialAvatarUrl,
    onSubmit,
    onCancel,
    isPending,
}: ProfileFormProps) {
    const {register, handleSubmit, formState: {errors}} = useForm<ProfileUpdateFormData>({
        resolver: zodResolver(profileUpdateSchema),
        defaultValues: {
            displayName: initialDisplayName,
            avatarUrl: initialAvatarUrl ?? '',
        },
    });

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <FormField label="Display Name" error={errors.displayName?.message}>
                <input
                    type="text"
                    {...register('displayName')}
                    className={inputClass(!!errors.displayName)}
                />
            </FormField>

            <FormField label="Avatar URL" error={errors.avatarUrl?.message}>
                <input
                    type="url"
                    {...register('avatarUrl')}
                    className={inputClass(!!errors.avatarUrl)}
                    placeholder="https://example.com/photo.jpg"
                />
            </FormField>

            <div className="flex gap-3 pt-2">
                <Button variant="primary" icon={Save} type="submit" disabled={isPending}>
                    {isPending ? 'Saving...' : 'Save'}
                </Button>
                <Button variant="outline" onClick={onCancel}>
                    Cancel
                </Button>
            </div>
        </form>
    );
}
