import {useProfile} from '@/hooks/useUser';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {ProfileSection} from '@/components/user/ProfileSection';
import {PasswordSection} from '@/components/user/PasswordSection';

export function ProfilePage() {
    const {data: user, isLoading, isError, error} = useProfile();
    if (isLoading) return <Spinner/>;
    if (isError) return <ErrorMessage error={error} fallback="Failed to load profile."/>;
    if (!user) return null;
    return (
        <div className="mx-auto max-w-md space-y-8">
            <h1 className="text-2xl font-bold text-foreground">Profile</h1>
            <ProfileSection user={user}/>
            <PasswordSection/>
        </div>
    );
}
