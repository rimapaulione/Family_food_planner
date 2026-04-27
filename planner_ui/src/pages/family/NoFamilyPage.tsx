import {useNavigate} from 'react-router-dom';
import {LogOut} from 'lucide-react';
import {useCreateFamily} from '@/hooks/useFamily';
import {useAuthStore} from '@/stores/useAuthStore';
import {CenteredCard} from '@/components/ui/CenteredCard';
import {CreateFamilyForm} from '@/components/family/CreateFamilyForm';
import type {FamilyCreateFormData} from '@/schemas/family';

export function NoFamilyPage() {
    const navigate = useNavigate();
    const clearAuth = useAuthStore((s) => s.clearAuth);
    const createMutation = useCreateFamily();

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
                <button
                    onClick={handleLogout}
                    className="inline-flex items-center gap-1 text-muted-foreground hover:text-foreground"
                >
                    <LogOut className="h-4 w-4"/> Logout
                </button>
            }
        >
            <CreateFamilyForm onSubmit={handleSubmit} isPending={createMutation.isPending}/>
        </CenteredCard>
    );
}
