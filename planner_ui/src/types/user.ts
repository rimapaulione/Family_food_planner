import type {Role} from '@/types/auth';

export interface UserResponse {
    id: string;
    email: string;
    displayName: string;
    avatarUrl: string | null;
    role: Role;
}

export interface UserUpdateRequest {
    displayName: string;
    avatarUrl: string | null;
}

export interface PasswordUpdateRequest {
    currentPassword: string;
    newPassword: string;
}
