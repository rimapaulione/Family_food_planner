import {z} from 'zod';

export const mealSlotServingsSchema = z.object({
    servings: z
        .string()
        .trim()
        .refine(
            (val) => val === '' || (/^\d+$/.test(val) && Number(val) >= 1 && Number(val) <= 50),
            'Must be 1-50 or empty',
        ),
});

export type MealSlotServingsFormData = z.infer<typeof mealSlotServingsSchema>;
