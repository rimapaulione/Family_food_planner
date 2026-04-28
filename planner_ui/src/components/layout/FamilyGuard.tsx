import {Navigate, Outlet} from 'react-router-dom';
import {useAuthStore} from '@/stores/useAuthStore';
import {useFamily} from '@/hooks/useFamily';
import {Spinner} from '@/components/ui/Spinner';
import {ROLE} from '@/types/auth';

export function FamilyGuard() {
    const familyId = useAuthStore((s) => s.familyId);
    const role = useAuthStore((s) => s.role);
    const {data: family, isLoading} = useFamily();

    if (familyId === null) return <Navigate to="/family/no-family" replace/>;
    if (isLoading) return <Spinner/>;
    if (family && !family.isSetupCompleted && role === ROLE.ADMIN) {
        return <Navigate to="/family/setup" replace/>;
    }
    return <Outlet/>;
}
