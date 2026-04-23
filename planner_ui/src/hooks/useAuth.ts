import {useMutation} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import type {AuthResponse, LoginRequest, RegisterRequest} from '@/types/auth';
import {getErrorMessage} from '@/utils/getErrorMessage';

const TOKEN_KEY = 'auth_token';

export function saveToken(token: string) {
    localStorage.setItem(TOKEN_KEY, token);
}

export function getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
}

export function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
}

export function useLogin() {
    return useMutation({
        mutationFn: async (request: LoginRequest) => {
            const {data} = await api.post<AuthResponse>('/auth/login', request);
            return data;
        },
        onSuccess: (data) => {
            saveToken(data.token);
            toast.success(`Welcome, ${data.displayName}`);
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Login failed')),
    });
}

export function useRegister() {
    return useMutation({
        mutationFn: async (request: RegisterRequest) => {
            const {data} = await api.post<AuthResponse>('/auth/register', request);
            return data;
        },
        onSuccess: (data) => {
            saveToken(data.token);
            toast.success(`Welcome, ${data.displayName}`);
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Registration failed')),
    });
}
