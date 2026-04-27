import {z} from 'zod';

const dayOfWeekValues = [
    'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY',
] as const;

const servingValue = z.number().int().min(1, 'Min 1').max(50, 'Max 50').nullable();

const mealServingsSchema = z.object({
    breakfast: servingValue,
    lunch: servingValue,
    dinner: servingValue,
});

export const familyCreateSchema = z.object({
    name: z.string().min(2, 'Min 2 characters').max(100, 'Max 100 characters'),
});

export const familyFormSchema = z.object({
    name: z.string().min(2, 'Min 2 characters').max(100, 'Max 100 characters'),
    shoppingDay: z.enum(dayOfWeekValues),
    defaultWeekdayServings: mealServingsSchema,
    defaultWeekendServings: mealServingsSchema,
});

export const familyUpdateSchema = familyFormSchema.extend({
    isSetupCompleted: z.boolean(),
});

export type FamilyCreateFormData = z.infer<typeof familyCreateSchema>;
export type FamilyFormData = z.infer<typeof familyFormSchema>;
export type FamilyUpdateFormData = z.infer<typeof familyUpdateSchema>;
