import {useMutation} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import type {AiRecipeGeneration} from '@/types/recipe';
import {getErrorMessage} from '@/utils/getErrorMessage';

interface RawAiMatchedIngredient {
    ingredientId: string;
    quantity: number;
    unit: string;
}

interface RawAiMissingIngredient {
    name: string;
    quantity: number;
    unit: string;
}

interface RawAiRecipeGeneration {
    name: string;
    categoryId: number;
    defaultServing: number;
    cookingTimeMinutes: number;
    notes: string;
    tagIds: number[];
    ingredients: RawAiMatchedIngredient[];
    missingIngredients: RawAiMissingIngredient[];
}

const toLowerUnit = <T extends {unit: string}>(item: T): T => ({
    ...item,
    unit: item.unit.toLowerCase(),
});

export function useGenerateRecipe() {
    return useMutation({
        mutationFn: async (prompt: string): Promise<AiRecipeGeneration> => {
            const {data} = await api.post<RawAiRecipeGeneration>('/ai/recipes/generate', {prompt});
            return {
                ...data,
                ingredients: data.ingredients.map(toLowerUnit),
                missingIngredients: data.missingIngredients.map(toLowerUnit),
            };
        },
        onError: (err) => toast.error(getErrorMessage(err, 'AI generation failed')),
    });
}
