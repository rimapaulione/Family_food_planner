import {z} from 'zod';

export const ingredientSchema = z.object({
    nameLt: z.string()
        .min(2, 'Name must be at least 2 characters')
        .max(100, 'Max 100 characters'),
    unit: z.string().min(1, 'Select a unit'),
});

export type IngredientFormData = z.infer<typeof ingredientSchema>;
