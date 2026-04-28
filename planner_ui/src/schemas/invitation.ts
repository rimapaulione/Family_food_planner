import {z} from 'zod';

export const invitationCreateSchema = z.object({
    email: z.string().email('Invalid email').max(255, 'Max 255 characters'),
});

export type InvitationCreateFormData = z.infer<typeof invitationCreateSchema>;
