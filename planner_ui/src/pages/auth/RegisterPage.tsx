import {useNavigate} from 'react-router-dom';
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
        <AuthCard
            title="Family Food Planner"
            subtitle="Create your account"
            footerPrompt="Already have an account?"
            footerLinkText="Log in"
            footerLinkTo="/login"
        >
            <RegisterForm onSubmit={handleSubmit} isPending={registerMutation.isPending}/>
        </AuthCard>
    );
}
