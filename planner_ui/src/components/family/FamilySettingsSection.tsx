import {useState} from 'react';
import {Settings} from 'lucide-react';
import {useUpdateFamily} from '@/hooks/useFamily';
import {SectionHeader} from '@/components/ui/SectionHeader';
import {FamilyForm} from '@/components/family/FamilyForm';
import {FamilyDetailsView} from '@/components/family/FamilyDetailsView';
import type {Family} from '@/types/family';
import type {FamilyFormData} from '@/schemas/family';

interface FamilySettingsSectionProps {
    family: Family;
    canEdit: boolean;
}

export function FamilySettingsSection({family, canEdit}: FamilySettingsSectionProps) {
    const [editing, setEditing] = useState(false);
    const updateMutation = useUpdateFamily();

    const handleSave = async (data: FamilyFormData) => {
        await updateMutation.mutateAsync({...data, isSetupCompleted: family.isSetupCompleted});
        setEditing(false);
    };

    return (
        <section className="space-y-3">
            <SectionHeader icon={Settings} title="Family Settings" level="h1"/>
            {editing ? (
                <FamilyForm
                    initialData={{
                        name: family.name,
                        shoppingDay: family.shoppingDay,
                        defaultWeekdayServings: family.defaultWeekdayServings,
                        defaultWeekendServings: family.defaultWeekendServings,
                        noRepeatRecipeDays: family.noRepeatRecipeDays,
                    }}
                    onSubmit={handleSave}
                    onCancel={() => setEditing(false)}
                    submitLabel="Save"
                    isPending={updateMutation.isPending}
                />
            ) : (
                <FamilyDetailsView
                    family={family}
                    canEdit={canEdit}
                    onEdit={() => setEditing(true)}
                />
            )}
        </section>
    );
}
