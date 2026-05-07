import {useNavigate} from 'react-router-dom';
import {useRegister} from '@/hooks/useAuth';
import {useAuthSearchParams} from '@/hooks/useAuthSearchParams';
import {AuthCard} from '@/components/auth/AuthCard';
import {RegisterForm} from '@/components/auth/RegisterForm';
import type {RegisterFormData} from '@/schemas/auth';

export function RegisterPage() {
    const navigate = useNavigate();
    const {redirectTo, email: lockedEmail, siblingHref} = useAuthSearchParams();
    const registerMutation = useRegister();

    const handleSubmit = async (data: RegisterFormData) => {
        const {confirmPassword: _unused, ...payload} = data;
        await registerMutation.mutateAsync(payload);
        navigate(redirectTo);
    };

    return (
        <AuthCard
            title="Family Food Planner"
            subtitle="Create your account"
            footerPrompt="Already have an account?"
            footerLinkText="Log in"
            footerLinkTo={siblingHref('/login')}
        >
            <RegisterForm
                onSubmit={handleSubmit}
                isPending={registerMutation.isPending}
                lockedEmail={lockedEmail}
            />
        </AuthCard>
    );
}
