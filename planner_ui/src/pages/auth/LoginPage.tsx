import {useNavigate} from 'react-router-dom';
import {useLogin} from '@/hooks/useAuth';
import {useAuthSearchParams} from '@/hooks/useAuthSearchParams';
import {AuthCard} from '@/components/auth/AuthCard';
import {LoginForm} from '@/components/auth/LoginForm';
import type {LoginFormData} from '@/schemas/auth';

export function LoginPage() {
    const navigate = useNavigate();
    const {redirectTo, email, siblingHref} = useAuthSearchParams();
    const loginMutation = useLogin();

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
            footerLinkTo={siblingHref('/register')}
        >
            <LoginForm
                onSubmit={handleSubmit}
                isPending={loginMutation.isPending}
                prefilledEmail={email}
            />
        </AuthCard>
    );
}
