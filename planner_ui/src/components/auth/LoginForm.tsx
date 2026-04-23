import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {LogIn} from 'lucide-react';
import {loginSchema, type LoginFormData} from '@/schemas/auth';
import {Button} from '@/components/ui/Button';
import {FormField} from '@/components/ui/FormField';
import {inputClass} from '@/utils/inputClass';

interface LoginFormProps {
    onSubmit: (data: LoginFormData) => Promise<void>;
    isPending: boolean;
}

export function LoginForm({onSubmit, isPending}: LoginFormProps) {
    const {register, handleSubmit, formState: {errors}} = useForm<LoginFormData>({
        resolver: zodResolver(loginSchema),
        defaultValues: {email: '', password: ''},
    });

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <FormField label="Email" error={errors.email?.message}>
                <input
                    type="email"
                    autoComplete="email"
                    {...register('email')}
                    className={inputClass(!!errors.email)}
                    placeholder="you@example.com"
                />
            </FormField>

            <FormField label="Password" error={errors.password?.message}>
                <input
                    type="password"
                    autoComplete="current-password"
                    {...register('password')}
                    className={inputClass(!!errors.password)}
                />
            </FormField>

            <Button variant="primary" icon={LogIn} type="submit" disabled={isPending} className="w-full justify-center">
                {isPending ? 'Logging in...' : 'Log in'}
            </Button>
        </form>
    );
}
