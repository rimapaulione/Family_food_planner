import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import {useAuthStore} from '@/stores/useAuthStore';
import {ROLE} from '@/types/auth';
import type {Role} from '@/types/auth';
import type {Family, FamilyCreateRequest, FamilyUpdateRequest} from '@/types/family';
import {getErrorMessage} from '@/utils/getErrorMessage';

export function useFamily() {
    const familyId = useAuthStore((s) => s.familyId);
    return useQuery({
        queryKey: ['family', 'me'],
        queryFn: async () => {
            const {data} = await api.get<Family>('/families/me');
            return data;
        },
        enabled: familyId !== null,
        staleTime: 0,
    });
}

export function useCreateFamily() {
    const queryClient = useQueryClient();
    const setFamilyId = useAuthStore((s) => s.setFamilyId);
    const setRole = useAuthStore((s) => s.setRole);
    return useMutation({
        mutationFn: async (request: FamilyCreateRequest) => {
            const {data} = await api.post<Family>('/families', request);
            return data;
        },
        onSuccess: (data) => {
            setFamilyId(data.id);
            setRole(ROLE.ADMIN);
            queryClient.setQueryData(['family', 'me'], data);
            toast.success('Family created');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to create family')),
    });
}

export function useUpdateFamily() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (request: FamilyUpdateRequest) => {
            const {data} = await api.patch<Family>('/families/me', request);
            return data;
        },
        onSuccess: (data) => {
            queryClient.setQueryData(['family', 'me'], data);
            toast.success('Family updated');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to update family')),
    });
}

export function useUpdateMemberRole() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({memberId, role}: {memberId: string; role: Role}) => {
            const {data} = await api.patch<Family>(
                `/families/members/${memberId}/role`,
                {role},
            );
            return data;
        },
        onSuccess: (data) => {
            queryClient.setQueryData(['family', 'me'], data);
            toast.success('Member role updated');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to update member role')),
    });
}
