import {useMutation} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import type {AuthResponse, LoginRequest, RegisterRequest} from '@/types/auth';
import {useAuthStore} from '@/stores/useAuthStore';
import {getErrorMessage} from '@/utils/getErrorMessage';

export function useLogin() {
    const setAuth = useAuthStore((s) => s.setAuth);
    return useMutation({
        mutationFn: async (request: LoginRequest) => {
            const {data} = await api.post<AuthResponse>('/auth/login', request);
            return data;
        },
        onSuccess: (data) => {
            setAuth(data);
            toast.success(`Welcome, ${data.displayName}`);
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Login failed')),
    });
}

export function useRegister() {
    const setAuth = useAuthStore((s) => s.setAuth);
    return useMutation({
        mutationFn: async (request: RegisterRequest) => {
            const {data} = await api.post<AuthResponse>('/auth/register', request);
            return data;
        },
        onSuccess: (data) => {
            setAuth(data);
            toast.success(`Welcome, ${data.displayName}`);
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Registration failed')),
    });
}
