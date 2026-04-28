import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import {useAuthStore} from '@/stores/useAuthStore';
import type {Family} from '@/types/family';
import type {Invitation, InvitationCreateRequest, InvitationPublic} from '@/types/invitation';
import {getErrorMessage} from '@/utils/getErrorMessage';

export function usePublicInvitation(token: string | undefined) {
    return useQuery({
        queryKey: ['invitations', 'public', token],
        queryFn: async () => {
            const {data} = await api.get<InvitationPublic>(`/invitations/public/${token}`);
            return data;
        },
        enabled: !!token,
        retry: false,
    });
}

export function useFamilyInvitations() {
    const role = useAuthStore((s) => s.role);
    return useQuery({
        queryKey: ['invitations', 'family'],
        queryFn: async () => {
            const {data} = await api.get<Invitation[]>('/invitations/family');
            return data;
        },
        enabled: role === 'ADMIN',
        staleTime: 0,
    });
}

export function useCreateInvitation() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (request: InvitationCreateRequest) => {
            const {data} = await api.post<Invitation>('/invitations', request);
            return data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['invitations', 'family']});
            toast.success('Invitation created');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to create invitation')),
    });
}

export function useCancelInvitation() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (id: string) => {
            await api.delete(`/invitations/${id}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['invitations', 'family']});
            toast.success('Invitation cancelled');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to cancel invitation')),
    });
}

export function useAcceptInvitation() {
    const queryClient = useQueryClient();
    const setFamilyId = useAuthStore((s) => s.setFamilyId);
    return useMutation({
        mutationFn: async (token: string) => {
            const {data} = await api.post<Family>(`/invitations/accept/${token}`);
            return data;
        },
        onSuccess: (data) => {
            setFamilyId(data.id);
            queryClient.setQueryData(['family', 'me'], data);
            toast.success('Joined family');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to accept invitation')),
    });
}
