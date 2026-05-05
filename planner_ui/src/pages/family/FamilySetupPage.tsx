import {Navigate, useNavigate} from 'react-router-dom';
import {useFamily, useUpdateFamily} from '@/hooks/useFamily';
import {useAuthStore} from '@/stores/useAuthStore';
import {CenteredCard} from '@/components/ui/CenteredCard';
import {Spinner} from '@/components/ui/Spinner';
import {FamilyForm} from '@/components/family/FamilyForm';
import type {FamilyFormData} from '@/schemas/family';
import {ROLE} from '@/types/auth';

export function FamilySetupPage() {
    const navigate = useNavigate();
    const role = useAuthStore((s) => s.role);
    const {data: family, isLoading} = useFamily();
    const updateMutation = useUpdateFamily();

    if (isLoading || !family) return <Spinner/>;
    if (family.isSetupCompleted) return <Navigate to="/" replace/>;
    if (role !== ROLE.ADMIN) return <Navigate to="/" replace/>;

    const handleSubmit = async (data: FamilyFormData) => {
        await updateMutation.mutateAsync({...data, isSetupCompleted: true});
        navigate('/');
    };

    return (
        <CenteredCard
            title="Welcome!"
            subtitle="Set up your family preferences"
            maxWidth="md"
        >
            <FamilyForm
                initialData={{
                    name: family.name,
                    shoppingDay: family.shoppingDay,
                    defaultWeekdayServings: family.defaultWeekdayServings,
                    defaultWeekendServings: family.defaultWeekendServings,
                    noRepeatRecipeDays: family.noRepeatRecipeDays,
                }}
                onSubmit={handleSubmit}
                submitLabel="Get Started"
                isPending={updateMutation.isPending}
            />
        </CenteredCard>
    );
}
