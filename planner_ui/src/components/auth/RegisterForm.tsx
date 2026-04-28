import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {UserPlus} from 'lucide-react';
import {registerSchema, type RegisterFormData} from '@/schemas/auth';
import {Button} from '@/components/ui/Button';
import {FormField} from '@/components/ui/FormField';
import {inputClass} from '@/utils/inputClass';

interface RegisterFormProps {
    onSubmit: (data: RegisterFormData) => Promise<void>;
    isPending: boolean;
    lockedEmail?: string;
}

export function RegisterForm({onSubmit, isPending, lockedEmail}: RegisterFormProps) {
    const {register, handleSubmit, formState: {errors}} = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
        defaultValues: {email: lockedEmail ?? '', password: '', confirmPassword: '', displayName: ''},
    });

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <FormField label="Name" error={errors.displayName?.message}>
                <input
                    type="text"
                    autoComplete="name"
                    {...register('displayName')}
                    className={inputClass(!!errors.displayName)}
                    placeholder="Your name"
                />
            </FormField>

            <FormField label="Email" error={errors.email?.message}>
                <input
                    type="email"
                    autoComplete="email"
                    readOnly={!!lockedEmail}
                    {...register('email')}
                    className={inputClass(!!errors.email)}
                    placeholder="you@example.com"
                />
            </FormField>

            <FormField label="Password" error={errors.password?.message}>
                <input
                    type="password"
                    autoComplete="new-password"
                    {...register('password')}
                    className={inputClass(!!errors.password)}
                    placeholder="At least 8 characters"
                />
            </FormField>

            <FormField label="Confirm password" error={errors.confirmPassword?.message}>
                <input
                    type="password"
                    autoComplete="new-password"
                    {...register('confirmPassword')}
                    className={inputClass(!!errors.confirmPassword)}
                />
            </FormField>

            <Button variant="primary" icon={UserPlus} type="submit" disabled={isPending} className="w-full justify-center">
                {isPending ? 'Creating account...' : 'Register'}
            </Button>
        </form>
    );
}
