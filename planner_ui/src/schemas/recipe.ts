import {z} from 'zod';

export const recipeSchema = z.object({
    name: z.string()
        .min(1, 'Recipe name is required')
        .max(100, 'Name max 100 characters'),
    categoryId: z.number()
        .min(1, 'Select a category'),
    defaultServing: z.number()
        .min(1, 'Min 1 serving')
        .max(50, 'Max 50 servings'),
    cookingTimeMinutes: z.number({message: 'Enter cooking time'})
        .min(1, 'Min 1 minute')
        .max(500, 'Max 500 minutes'),
    tagIds: z.array(z.number()).optional(),
    leftoverRecipeId: z.string().nullable().optional(),
    isFavorite: z.boolean(),
    notes: z.string()
        .max(1000, 'Notes max 1000 characters')
        .optional(),
    ingredients: z.array(
        z.object({
            ingredientId: z.string(),
            quantity: z.number(),
            unit: z.string(),
        }),
    ).superRefine((rows, ctx) => {
        rows.forEach((row, i) => {
            if (!row.ingredientId) return;
            if (row.quantity < 0.01) {
                ctx.addIssue({
                    code: 'custom',
                    path: [i, 'quantity'],
                    message: 'Min quantity 0.01',
                });
            }
            if (row.quantity > 99999) {
                ctx.addIssue({
                    code: 'custom',
                    path: [i, 'quantity'],
                    message: 'Quantity too large',
                });
            }
        });
    }).optional(),
});

export type RecipeFormData = z.infer<typeof recipeSchema>;
