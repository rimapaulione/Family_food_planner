import {useState} from 'react';
import {Users} from 'lucide-react';
import {SectionHeader} from '@/components/ui/SectionHeader';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import {FamilyMemberCard} from '@/components/family/FamilyMemberCard';
import {useUpdateMemberRole} from '@/hooks/useFamily';
import {useAuthStore} from '@/stores/useAuthStore';
import {ROLE} from '@/types/auth';
import type {FamilyMember} from '@/types/family';

interface FamilyMembersSectionProps {
    members: FamilyMember[];
}

export function FamilyMembersSection({members}: FamilyMembersSectionProps) {
    const userId = useAuthStore((s) => s.id);
    const role = useAuthStore((s) => s.role);
    const isAdmin = role === ROLE.ADMIN;
    const [toToggle, setToToggle] = useState<FamilyMember | null>(null);
    const updateRoleMutation = useUpdateMemberRole();

    const targetRole = toToggle?.role === ROLE.ADMIN ? ROLE.USER : ROLE.ADMIN;
    const actionLabel = toToggle?.role === ROLE.ADMIN ? 'Demote to member' : 'Promote to admin';
    const description = toToggle
        ? toToggle.role === ROLE.ADMIN
            ? `Demote ${toToggle.displayName} from admin to member?`
            : `Promote ${toToggle.displayName} from member to admin?`
        : '';

    return (
        <section className="space-y-3">
            <SectionHeader icon={Users} title={`Members (${members.length})`}/>
            <ul className="space-y-2">
                {members.map((member) => {
                    const isSelf = member.userId === userId;
                    return (
                        <li key={member.userId}>
                            <FamilyMemberCard
                                member={member}
                                isCurrentUser={isSelf}
                                canEditRole={isAdmin && !isSelf}
                                onRoleClick={() => setToToggle(member)}
                            />
                        </li>
                    );
                })}
            </ul>

            <ConfirmDialog
                open={toToggle !== null}
                onOpenChange={(open) => { if (!open) setToToggle(null); }}
                title="Change member role?"
                description={description}
                confirmText={actionLabel}
                onConfirm={() => {
                    if (toToggle) {
                        updateRoleMutation.mutate({memberId: toToggle.userId, role: targetRole});
                    }
                    setToToggle(null);
                }}
            />
        </section>
    );
}
