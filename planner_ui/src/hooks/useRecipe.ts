import {useQuery, useMutation, useQueryClient} from '@tanstack/react-query';
import api from '@/api/axios';
import type {RecipeListResponse, RecipeResponse, RecipeRequest} from '@/types/recipe';

export function useRecipes(search?: string) {
    return useQuery({
        queryKey: ['recipes', search],
        queryFn: async () => {
            const {data} = await api.get<RecipeListResponse[]>('/recipes', {
                params: search ? {search} : {},
            });
            return data;
        },
    });
}

export function useRecipe(id: string) {
    return useQuery({
        queryKey: ['recipes', id],
        queryFn: async () => {
            const {data} = await api.get<RecipeResponse>(`/recipes/${id}`);
            return data;
        },
        enabled: !!id,
    });
}

export function useCreateRecipe() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (recipe: RecipeRequest) => {
            const {data} = await api.post<RecipeResponse>('/recipes', recipe);
            return data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['recipes']});
        },
    });
}

export function useUpdateRecipe(id: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (recipe: RecipeRequest) => {
            const {data} = await api.put<RecipeResponse>(`/recipes/${id}`, recipe);
            return data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['recipes']});
        },
    });
}

export function useDeleteRecipe() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (id: string) => {
            await api.delete(`/recipes/${id}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['recipes']});
        },
    });
}
