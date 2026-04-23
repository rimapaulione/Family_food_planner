import {useForm, Controller} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {UserPlus} from 'lucide-react';
import {registerSchema, type RegisterFormData} from '@/schemas/auth';
import {Button} from '@/components/ui/Button';
import {FormField} from '@/components/ui/FormField';
import {inputClass} from '@/utils/inputClass';

interface RegisterFormProps {
    onSubmit: (data: RegisterFormData) => Promise<void>;
    isPending: boolean;
}

export function RegisterForm({onSubmit, isPending}: RegisterFormProps) {
    const {register, handleSubmit, control, formState: {errors}} = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
        defaultValues: {email: '', password: '', confirmPassword: '', displayName: ''},
    });

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <FormField label="Name" error={errors.displayName?.message}>
                <Controller
                    control={control}
                    name="displayName"
                    render={({field}) => (
                        <input
                            type="text"
                            autoComplete="name"
                            value={field.value}
                            onChange={(e) => {
                                const val = e.target.value;
                                field.onChange(val ? val.charAt(0).toUpperCase() + val.slice(1) : '');
                            }}
                            className={inputClass(!!errors.displayName)}
                            placeholder="Your name"
                        />
                    )}
                />
            </FormField>

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
