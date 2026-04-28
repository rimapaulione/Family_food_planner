import {useFamily} from '@/hooks/useFamily';
import {useAuthStore} from '@/stores/useAuthStore';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {FamilySettingsSection} from '@/components/family/FamilySettingsSection';
import {FamilyMembersSection} from '@/components/family/FamilyMembersSection';
import {FamilyInviteSection} from '@/components/family/FamilyInviteSection';
import {ROLE} from '@/types/auth';

export function FamilyPage() {
    const role = useAuthStore((s) => s.role);
    const {data: family, isLoading, isError, error} = useFamily();
    const isAdmin = role === ROLE.ADMIN;

    if (isLoading) return <Spinner/>;
    if (isError) return <ErrorMessage error={error} fallback="Failed to load family."/>;
    if (!family) return null;

    return (
        <div className="mx-auto max-w-2xl space-y-8">
            <FamilySettingsSection family={family} canEdit={isAdmin}/>
            <FamilyMembersSection members={family.members}/>
            {isAdmin && <FamilyInviteSection/>}
        </div>
    );
}
