import {useNavigate, useSearchParams} from 'react-router-dom';
import {useRegister} from '@/hooks/useAuth';
import {AuthCard} from '@/components/auth/AuthCard';
import {RegisterForm} from '@/components/auth/RegisterForm';
import type {RegisterFormData} from '@/schemas/auth';

export function RegisterPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get('redirectTo') ?? '/';
    const lockedEmail = searchParams.get('email') ?? undefined;
    const registerMutation = useRegister();

    const loginHref = lockedEmail || redirectTo !== '/'
        ? `/login?${searchParams.toString()}`
        : '/login';

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
            footerLinkTo={loginHref}
        >
            <RegisterForm
                onSubmit={handleSubmit}
                isPending={registerMutation.isPending}
                lockedEmail={lockedEmail}
            />
        </AuthCard>
    );
}
