import {Avatar} from '@/components/ui/Avatar';
import {Card} from '@/components/ui/Card';
import type {FamilyMember} from '@/types/family';

interface FamilyMemberCardProps {
    member: FamilyMember;
    isCurrentUser: boolean;
    canEditRole: boolean;
    onRoleClick?: () => void;
}

export function FamilyMemberCard({member, isCurrentUser, canEditRole, onRoleClick}: FamilyMemberCardProps) {
    const initial = member.displayName.charAt(0).toUpperCase();
    const roleClass = member.role === 'ADMIN'
        ? 'bg-primary text-primary-foreground'
        : 'bg-secondary text-secondary-foreground';
    const badgeClasses = `flex-none rounded-full px-2 py-0.5 text-xs ${roleClass}`;

    return (
        <Card padding="sm">
            <div className="flex items-center justify-between gap-3">
                <div className="flex min-w-0 items-center gap-3">
                    <Avatar src={member.avatarUrl} size="sm" initials={initial}/>
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
                {canEditRole ? (
                    <button
                        type="button"
                        onClick={onRoleClick}
                        className={`${badgeClasses} cursor-pointer hover:opacity-80`}
                    >
                        {member.role}
                    </button>
                ) : (
                    <span className={badgeClasses}>{member.role}</span>
                )}
            </div>
        </Card>
    );
}
