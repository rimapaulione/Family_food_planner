import {useState} from 'react';
import {useChangePassword} from '@/hooks/useUser';
import {PasswordForm} from '@/components/user/PasswordForm';
import {PasswordView} from '@/components/user/PasswordView';
import type {PasswordChangeFormData} from '@/schemas/user';

export function PasswordSection() {
    const [changing, setChanging] = useState(false);
    const passwordMutation = useChangePassword();

    const changePassword = async (data: PasswordChangeFormData) => {
        await passwordMutation.mutateAsync({
            currentPassword: data.currentPassword,
            newPassword: data.newPassword,
        });
        setChanging(false);
    };

    if (changing) {
        return (
            <PasswordForm
                isPending={passwordMutation.isPending}
                onCancel={() => setChanging(false)}
                onSubmit={changePassword}
            />
        );
    }

    return <PasswordView onChange={() => setChanging(true)}/>;
}
