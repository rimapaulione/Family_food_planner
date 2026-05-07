import {Navigate, useNavigate} from 'react-router-dom';
import {LogOut} from 'lucide-react';
import {useCreateFamily} from '@/hooks/useFamily';
import {useAuthStore} from '@/stores/useAuthStore';
import {Button} from '@/components/ui/Button';
import {CenteredCard} from '@/components/ui/CenteredCard';
import {CreateFamilyForm} from '@/components/family/CreateFamilyForm';
import type {FamilyCreateFormData} from '@/schemas/family';

export function NoFamilyPage() {
    const navigate = useNavigate();
    const familyId = useAuthStore((s) => s.familyId);
    const clearAuth = useAuthStore((s) => s.clearAuth);
    const createMutation = useCreateFamily();

    if (familyId !== null) return <Navigate to="/" replace/>;

    const handleSubmit = async (data: FamilyCreateFormData) => {
        await createMutation.mutateAsync(data);
        navigate('/family/setup');
    };

    const handleLogout = () => {
        clearAuth();
        navigate('/login');
    };

    return (
        <CenteredCard
            title="No Family"
            subtitle="Create a new family or wait for an invitation"
            footer={
                <Button variant="ghost" icon={LogOut} onClick={handleLogout}>
                    Logout
                </Button>
            }
        >
            <CreateFamilyForm onSubmit={handleSubmit} isPending={createMutation.isPending}/>
        </CenteredCard>
    );
}
