import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {KeyRound} from 'lucide-react';
import {passwordChangeSchema, type PasswordChangeFormData} from '@/schemas/user';
import {Button} from '@/components/ui/Button';
import {FormField} from '@/components/ui/FormField';
import {inputClass} from '@/utils/inputClass';

interface PasswordFormProps {
    onSubmit: (data: PasswordChangeFormData) => Promise<void>;
    onCancel: () => void;
    isPending: boolean;
}

export function PasswordForm({onSubmit, onCancel, isPending}: PasswordFormProps) {
    const {register, handleSubmit, reset, formState: {errors}} = useForm<PasswordChangeFormData>({
        resolver: zodResolver(passwordChangeSchema),
        defaultValues: {currentPassword: '', newPassword: '', confirmPassword: ''},
    });

    const handleFormSubmit = handleSubmit(async (data) => {
        await onSubmit(data);
        reset();
    });

    return (
        <form onSubmit={handleFormSubmit} className="space-y-4">
            <FormField label="Current Password" error={errors.currentPassword?.message}>
                <input
                    type="password"
                    autoComplete="current-password"
                    {...register('currentPassword')}
                    className={inputClass(!!errors.currentPassword)}
                />
            </FormField>

            <FormField label="New Password" error={errors.newPassword?.message}>
                <input
                    type="password"
                    autoComplete="new-password"
                    {...register('newPassword')}
                    className={inputClass(!!errors.newPassword)}
                />
            </FormField>

            <FormField label="Confirm New Password" error={errors.confirmPassword?.message}>
                <input
                    type="password"
                    autoComplete="new-password"
                    {...register('confirmPassword')}
                    className={inputClass(!!errors.confirmPassword)}
                />
            </FormField>

            <div className="flex gap-3 pt-2">
                <Button variant="primary" icon={KeyRound} type="submit" disabled={isPending}>
                    {isPending ? 'Changing...' : 'Change Password'}
                </Button>
                <Button variant="outline" onClick={onCancel}>
                    Cancel
                </Button>
            </div>
        </form>
    );
}
