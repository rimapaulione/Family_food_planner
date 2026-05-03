import {useQuery, useMutation, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import type {RecipeListResponse, RecipeResponse, RecipeRequest} from '@/types/recipe';
import {getErrorMessage} from '@/utils/getErrorMessage';

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

export function useRecipesById(id: string) {
    return useQuery({
        queryKey: ['recipes', id],
        queryFn: async () => {
            const {data} = await api.get<RecipeResponse>(`/recipes/${id}`);
            return {
                ...data,
                ingredients: data.ingredients.map((ing) => ({
                    ...ing,
                    unit: (ing.unit ?? '').toLowerCase(),
                })),
            };
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
            toast.success('Recipe created');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to create recipe')),
    });
}

export function useUpdateRecipe() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({id, data}: {id: string; data: RecipeRequest}) => {
            const {data: updated} = await api.put<RecipeResponse>(`/recipes/${id}`, data);
            return updated;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['recipes']});
            toast.success('Recipe updated');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to update recipe')),
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
            toast.success('Recipe deleted');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to delete recipe')),
    });
}
