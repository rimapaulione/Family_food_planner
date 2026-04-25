import {useState} from 'react';
import {useUpdateProfile} from '@/hooks/useUser';
import {ProfileForm} from '@/components/user/ProfileForm';
import {ProfileView} from '@/components/user/ProfileView';
import type {UserResponse} from '@/types/user';
import type {ProfileUpdateFormData} from '@/schemas/user';

interface ProfileSectionProps {
    user: UserResponse;
}

export function ProfileSection({user}: ProfileSectionProps) {
    const [editing, setEditing] = useState(false);
    const updateMutation = useUpdateProfile();

    const saveProfile = async (data: ProfileUpdateFormData) => {
        await updateMutation.mutateAsync({
            displayName: data.displayName,
            avatarUrl: data.avatarUrl || null,
        });
        setEditing(false);
    };

    if (editing) {
        return (
            <ProfileForm
                initialDisplayName={user.displayName}
                initialAvatarUrl={user.avatarUrl}
                isPending={updateMutation.isPending}
                onCancel={() => setEditing(false)}
                onSubmit={saveProfile}
            />
        );
    }

    return (
        <ProfileView
            displayName={user.displayName}
            email={user.email}
            avatarUrl={user.avatarUrl}
            onEdit={() => setEditing(true)}
        />
    );
}
