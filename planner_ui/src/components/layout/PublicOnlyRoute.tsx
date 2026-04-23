import {Navigate, Outlet} from 'react-router-dom';
import {useAuthStore} from '@/stores/useAuthStore';

export function PublicOnlyRoute() {
    const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

    if (isAuthenticated) {
        return <Navigate to="/" replace/>;
    }
    return <Outlet/>;
}
