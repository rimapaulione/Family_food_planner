import {useNavigate} from 'react-router-dom';
import {useLogin} from '@/hooks/useAuth';
import {AuthCard} from '@/components/auth/AuthCard';
import {LoginForm} from '@/components/auth/LoginForm';
import type {LoginFormData} from '@/schemas/auth';

export function LoginPage() {
    const navigate = useNavigate();
    const loginMutation = useLogin();

    const handleSubmit = async (data: LoginFormData) => {
        await loginMutation.mutateAsync(data);
        navigate('/');
    };

    return (
        <AuthCard
            title="Family Food Planner"
            subtitle="Sign in to your account"
            footerPrompt="No account?"
            footerLinkText="Register"
            footerLinkTo="/register"
        >
            <LoginForm onSubmit={handleSubmit} isPending={loginMutation.isPending}/>
        </AuthCard>
    );
}
