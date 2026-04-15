import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import type {Ingredient, IngredientDetail} from '@/types/ingredient';
import {getErrorMessage} from '@/utils/getErrorMessage';

export interface IngredientRequest {
    nameLt: string;
    unit: string;
}

const toLowerUnit = <T extends {unit: string}>(item: T): T => ({
    ...item,
    unit: item.unit.toLowerCase(),
});

const toUpperUnit = (data: IngredientRequest): IngredientRequest => ({
    ...data,
    unit: data.unit.toUpperCase(),
});

export function useIngredients(search?: string) {
    return useQuery({
        queryKey: ['ingredients', search],
        queryFn: async () => {
            const {data} = await api.get<Ingredient[]>('/ingredients', {
                params: search ? {search} : {},
            });
            return data.map(toLowerUnit);
        },
    });
}

export function useIngredientsWithRecipeCount(search?: string) {
    return useQuery({
        queryKey: ['ingredients', 'with-recipes', search],
        queryFn: async () => {
            const {data} = await api.get<IngredientDetail[]>('/ingredients/with-recipes-count', {
                params: search ? {search} : {},
            });
            return data.map(toLowerUnit);
        },
    });
}

export function useCreateIngredient() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (ingredient: IngredientRequest) => {
            const {data} = await api.post<Ingredient>('/ingredients', toUpperUnit(ingredient));
            return toLowerUnit(data);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['ingredients']});
            toast.success('Ingredient added');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to add ingredient')),
    });
}

export function useUpdateIngredient() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({id, data}: {id: string; data: IngredientRequest}) => {
            const {data: updated} = await api.put<Ingredient>(`/ingredients/${id}`, toUpperUnit(data));
            return toLowerUnit(updated);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['ingredients']});
            toast.success('Ingredient updated');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to update ingredient')),
    });
}

export function useDeleteIngredient() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (id: string) => {
            await api.delete(`/ingredients/${id}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['ingredients']});
            toast.success('Ingredient deleted');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to delete ingredient')),
    });
}
