import {Link, useNavigate} from 'react-router-dom';
import {useRegister} from '@/hooks/useAuth';
import {AuthCard} from '@/components/auth/AuthCard';
import {RegisterForm} from '@/components/auth/RegisterForm';
import type {RegisterFormData} from '@/schemas/auth';

export function RegisterPage() {
    const navigate = useNavigate();
    const registerMutation = useRegister();

    const handleSubmit = async (data: RegisterFormData) => {
        const {confirmPassword: _unused, ...payload} = data;
        await registerMutation.mutateAsync(payload);
        navigate('/');
    };

    return (
        <AuthCard title="Create account">
            <RegisterForm onSubmit={handleSubmit} isPending={registerMutation.isPending}/>
            <p className="text-center text-sm">
                Already have an account?{' '}
                <Link to="/login" className="font-medium text-primary hover:underline">
                    Log in
                </Link>
            </p>
        </AuthCard>
    );
}
