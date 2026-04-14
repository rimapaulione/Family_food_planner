import {useQuery} from '@tanstack/react-query';
import api from '@/api/axios';
import type {Tag} from '@/types/recipe';

export function useTags() {
    return useQuery({
        queryKey: ['tags'],
        queryFn: async () => {
            const {data} = await api.get<Tag[]>('/tags');
            return data;
        },
    });
}
