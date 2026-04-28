import {Users} from 'lucide-react';
import {SectionHeader} from '@/components/ui/SectionHeader';
import {FamilyMemberCard} from '@/components/family/FamilyMemberCard';
import {useAuthStore} from '@/stores/useAuthStore';
import type {FamilyMember} from '@/types/family';

interface FamilyMembersSectionProps {
    members: FamilyMember[];
}

export function FamilyMembersSection({members}: FamilyMembersSectionProps) {
    const userId = useAuthStore((s) => s.id);
    return (
        <section className="space-y-3">
            <SectionHeader icon={Users} title={`Members (${members.length})`}/>
            <ul className="space-y-2">
                {members.map((member) => (
                    <li key={member.userId}>
                        <FamilyMemberCard
                            member={member}
                            isCurrentUser={member.userId === userId}
                        />
                    </li>
                ))}
            </ul>
        </section>
    );
}
