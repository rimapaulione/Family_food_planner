import {create} from 'zustand';
import {persist} from 'zustand/middleware';
import type {AuthResponse, Role} from '@/types/auth';

interface AuthState {
    token: string | null;
    id: string | null;
    email: string | null;
    displayName: string | null;
    avatarUrl: string | null;
    role: Role | null;
    familyId: string | null;
    isAuthenticated: boolean;
    setAuth: (data: AuthResponse) => void;
    setProfile: (data: {displayName: string; avatarUrl: string | null}) => void;
    setFamilyId: (familyId: string | null) => void;
    clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            token: null,
            id: null,
            email: null,
            displayName: null,
            avatarUrl: null,
            role: null,
            familyId: null,
            isAuthenticated: false,
            setAuth: (data) =>
                set({
                    token: data.token,
                    id: data.id,
                    email: data.email,
                    displayName: data.displayName,
                    avatarUrl: data.avatarUrl,
                    role: data.role,
                    familyId: data.familyId,
                    isAuthenticated: true,
                }),
            setProfile: (data) =>
                set({
                    displayName: data.displayName,
                    avatarUrl: data.avatarUrl,
                }),
            setFamilyId: (familyId) => set({familyId}),
            clearAuth: () =>
                set({
                    token: null,
                    id: null,
                    email: null,
                    displayName: null,
                    avatarUrl: null,
                    role: null,
                    familyId: null,
                    isAuthenticated: false,
                }),
        }),
        {name: 'auth-storage'},
    ),
);
