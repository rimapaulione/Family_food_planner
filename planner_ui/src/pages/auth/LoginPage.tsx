import {Link, useNavigate} from 'react-router-dom';
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
        <AuthCard title="Log in">
            <LoginForm onSubmit={handleSubmit} isPending={loginMutation.isPending}/>
            <p className="text-center text-sm">
                No account?{' '}
                <Link to="/register" className="font-medium text-primary hover:underline">
                    Register
                </Link>
            </p>
        </AuthCard>
    );
}
