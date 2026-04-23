import {create} from 'zustand';
import {persist} from 'zustand/middleware';
import type {AuthResponse, Role} from '@/types/auth';

interface AuthState {
    token: string | null;
    userId: string | null;
    email: string | null;
    displayName: string | null;
    role: Role | null;
    isAuthenticated: boolean;
    setAuth: (data: AuthResponse) => void;
    clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            token: null,
            userId: null,
            email: null,
            displayName: null,
            role: null,
            isAuthenticated: false,
            setAuth: (data) =>
                set({
                    token: data.token,
                    userId: data.userId,
                    email: data.email,
                    displayName: data.displayName,
                    role: data.role,
                    isAuthenticated: true,
                }),
            clearAuth: () =>
                set({
                    token: null,
                    userId: null,
                    email: null,
                    displayName: null,
                    role: null,
                    isAuthenticated: false,
                }),
        }),
        {name: 'auth-storage'},
    ),
);
