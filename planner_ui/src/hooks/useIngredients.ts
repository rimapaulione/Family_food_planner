import { useQuery } from '@tanstack/react-query';
import api from '@/api/axios';
import type { Ingredient, IngredientDetail } from '@/types/ingredient';

export function useIngredients(search?: string) {
  return useQuery({
    queryKey: ['ingredients', search],
    queryFn: async () => {
      const { data } = await api.get<Ingredient[]>('/ingredients', {
        params: search ? { search } : {},
      });
      return data;
    },
  });
}

export function useIngredientsWithRecipeCount(search?: string) {
  return useQuery({
    queryKey: ['ingredients', 'with-recipes', search],
    queryFn: async () => {
      const { data } = await api.get<IngredientDetail[]>('/ingredients/with-recipes-count', {
        params: search ? { search } : {},
      });
      return data;
    },
  });
}
