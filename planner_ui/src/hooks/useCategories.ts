import {useQuery} from '@tanstack/react-query';
import api from '@/api/axios';
import type {Category} from '@/types/recipe';

export function useCategories() {
    return useQuery({
        queryKey: ['categories'],
        queryFn: async () => {
            const {data} = await api.get<Category[]>('/categories');
            return data;
        },
    });
}
