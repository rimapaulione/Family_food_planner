
export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
    displayName: string;
}

export type Role = 'USER' | 'ADMIN';

export interface AuthResponse {
    token: string;
    id: string;
    email: string;
    displayName: string;
    avatarUrl: string | null;
    role: Role;
    familyId: string | null;
}
