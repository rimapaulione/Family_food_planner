import {z} from 'zod';

export const profileUpdateSchema = z.object({
    displayName: z.string().min(1, 'Name is required').max(100, 'Max 100 characters'),
    avatarUrl: z.string().max(512, 'URL too long').url('Must be a valid URL').or(z.literal('')),
});

export const passwordChangeSchema = z.object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: z.string().min(8, 'Min 8 characters').max(72, 'Max 72 characters'),
    confirmPassword: z.string().min(1, 'Confirm your new password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ['confirmPassword'],
}).refine((data) => data.currentPassword !== data.newPassword, {
    message: 'New password must differ from current',
    path: ['newPassword'],
});

export type ProfileUpdateFormData = z.infer<typeof profileUpdateSchema>;
export type PasswordChangeFormData = z.infer<typeof passwordChangeSchema>;
