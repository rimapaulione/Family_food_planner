import {useState} from 'react';
import {Copy, X, Check} from 'lucide-react';
import {toast} from 'sonner';
import {Card} from '@/components/ui/Card';
import {Button} from '@/components/ui/Button';
import type {Invitation} from '@/types/invitation';

interface PendingInvitationCardProps {
    invitation: Invitation;
    onCancel: () => void;
}

export function PendingInvitationCard({invitation, onCancel}: PendingInvitationCardProps) {
    const [copied, setCopied] = useState(false);

    const copyLink = async () => {
        const link = `${window.location.origin}/join/${invitation.token}`;
        try {
            await navigator.clipboard.writeText(link);
            setCopied(true);
            toast.success('Invite link copied');
            setTimeout(() => setCopied(false), 2000);
        } catch {
            toast.error('Failed to copy link');
        }
    };

    return (
        <Card variant="dashed" padding="sm">
            <div className="flex items-center justify-between gap-2">
                <span className="truncate text-sm text-foreground">{invitation.invitedEmail}</span>
                <div className="flex flex-none items-center gap-2">
                    <Button
                        variant="outline"
                        icon={copied ? Check : Copy}
                        onClick={copyLink}
                    >
                        <span className="hidden sm:inline">{copied ? 'Copied' : 'Copy link'}</span>
                    </Button>
                    <Button variant="destructive" icon={X} onClick={onCancel}>
                        <span className="hidden sm:inline">Cancel</span>
                    </Button>
                </div>
            </div>
        </Card>
    );
}
