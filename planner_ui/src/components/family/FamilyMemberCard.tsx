import {Card} from '@/components/ui/Card';
import type {FamilyMember} from '@/types/family';

interface FamilyMemberCardProps {
    member: FamilyMember;
    isCurrentUser: boolean;
}

export function FamilyMemberCard({member, isCurrentUser}: FamilyMemberCardProps) {
    const initial = member.displayName.charAt(0).toUpperCase();
    const roleClass = member.role === 'ADMIN'
        ? 'bg-primary text-primary-foreground'
        : 'bg-secondary text-secondary-foreground';

    return (
        <Card padding="sm">
            <div className="flex items-center justify-between gap-3">
                <div className="flex min-w-0 items-center gap-3">
                    <div className="flex h-8 w-8 flex-none items-center justify-center rounded-full bg-secondary text-xs font-bold text-secondary-foreground">
                        {initial}
                    </div>
                    <div className="min-w-0">
                        <p className="truncate text-sm font-medium text-foreground">
                            {member.displayName}
                            {isCurrentUser && (
                                <span className="ml-1 text-xs text-muted-foreground">(you)</span>
                            )}
                        </p>
                        <p className="truncate text-xs text-muted-foreground">{member.email}</p>
                    </div>
                </div>
                <span className={`flex-none rounded-full px-2 py-0.5 text-xs ${roleClass}`}>
                    {member.role}
                </span>
            </div>
        </Card>
    );
}
