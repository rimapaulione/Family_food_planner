import {Trash2} from 'lucide-react';
import {Avatar} from '@/components/ui/Avatar';
import {Card} from '@/components/ui/Card';
import type {FamilyMember} from '@/types/family';

interface FamilyMemberCardProps {
    member: FamilyMember;
    isCurrentUser: boolean;
    canEditRole: boolean;
    canRemove: boolean;
    onRoleClick?: () => void;
    onRemoveClick?: () => void;
}

export function FamilyMemberCard({
    member,
    isCurrentUser,
    canEditRole,
    canRemove,
    onRoleClick,
    onRemoveClick,
}: FamilyMemberCardProps) {
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
                <div className="flex items-center gap-2">
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
                    {canRemove && (
                        <button
                            type="button"
                            onClick={onRemoveClick}
                            aria-label="Remove member"
                            className="rounded p-1 text-muted-foreground hover:bg-destructive hover:text-destructive-foreground"
                        >
                            <Trash2 className="h-4 w-4"/>
                        </button>
                    )}
                </div>
            </div>
        </Card>
    );
}
