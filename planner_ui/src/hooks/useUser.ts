import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import {useAuthStore} from '@/stores/useAuthStore';
import type {PasswordUpdateRequest, UserResponse, UserUpdateRequest} from '@/types/user';
import {getErrorMessage} from '@/utils/getErrorMessage';

export function useProfile() {
    return useQuery({
        queryKey: ['user', 'me'],
        queryFn: async () => {
            const {data} = await api.get<UserResponse>('/users/me');
            return data;
        },
    });
}

export function useUpdateProfile() {
    const queryClient = useQueryClient();
    const setProfile = useAuthStore((s) => s.setProfile);
    return useMutation({
        mutationFn: async (request: UserUpdateRequest) => {
            const {data} = await api.put<UserResponse>('/users/me', request);
            return data;
        },
        onSuccess: (data) => {
            queryClient.setQueryData(['user', 'me'], data);
            setProfile({displayName: data.displayName, avatarUrl: data.avatarUrl});
            toast.success('Profile updated');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to update profile')),
    });
}

export function useChangePassword() {
    return useMutation({
        mutationFn: async (request: PasswordUpdateRequest) => {
            await api.put('/users/me/password', request);
        },
        onSuccess: () => toast.success('Password changed'),
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to change password')),
    });
}
