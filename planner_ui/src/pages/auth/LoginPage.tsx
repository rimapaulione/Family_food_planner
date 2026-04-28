import {useNavigate, useSearchParams} from 'react-router-dom';
import {useLogin} from '@/hooks/useAuth';
import {AuthCard} from '@/components/auth/AuthCard';
import {LoginForm} from '@/components/auth/LoginForm';
import type {LoginFormData} from '@/schemas/auth';

export function LoginPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const redirectTo = searchParams.get('redirectTo') ?? '/';
    const prefilledEmail = searchParams.get('email') ?? undefined;
    const loginMutation = useLogin();

    const registerHref = prefilledEmail || redirectTo !== '/'
        ? `/register?${searchParams.toString()}`
        : '/register';

    const handleSubmit = async (data: LoginFormData) => {
        await loginMutation.mutateAsync(data);
        navigate(redirectTo);
    };

    return (
        <AuthCard
            title="Family Food Planner"
            subtitle="Sign in to your account"
            footerPrompt="No account?"
            footerLinkText="Register"
            footerLinkTo={registerHref}
        >
            <LoginForm
                onSubmit={handleSubmit}
                isPending={loginMutation.isPending}
                prefilledEmail={prefilledEmail}
            />
        </AuthCard>
    );
}
