import {useNavigate, useParams} from 'react-router-dom';
import {LogOut, LogIn, UserPlus, Check} from 'lucide-react';
import {useAuthStore} from '@/stores/useAuthStore';
import {usePublicInvitation, useAcceptInvitation} from '@/hooks/useInvitations';
import {CenteredCard} from '@/components/ui/CenteredCard';
import {Button} from '@/components/ui/Button';
import {Spinner} from '@/components/ui/Spinner';

export function JoinPage() {
    const {token} = useParams<{token: string}>();
    const navigate = useNavigate();
    const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
    const userEmail = useAuthStore((s) => s.email);
    const clearAuth = useAuthStore((s) => s.clearAuth);

    const {data: invitation, isLoading, isError} = usePublicInvitation(token);
    const acceptMutation = useAcceptInvitation();

    if (isLoading) return <Spinner/>;

    if (isError || !invitation) {
        return (
            <CenteredCard title="Invitation not found" subtitle="This link is invalid or has been removed."/>
        );
    }

    if (invitation.status !== 'PENDING') {
        return (
            <CenteredCard
                title="Invitation unavailable"
                subtitle={`This invitation is ${invitation.status.toLowerCase()}.`}
            />
        );
    }

    const subtitle = `You're invited to join ${invitation.familyName} as ${invitation.invitedEmail}`;

    if (!isAuthenticated) {
        const params = new URLSearchParams({
            email: invitation.invitedEmail,
            redirectTo: `/join/${token}`,
        }).toString();

        return (
            <CenteredCard title="Family invitation" subtitle={subtitle}>
                <div className="flex flex-col gap-3">
                    <Button
                        variant="primary"
                        icon={UserPlus}
                        to={`/register?${params}`}
                        className="justify-center"
                    >
                        Create account
                    </Button>
                    <Button
                        variant="outline"
                        icon={LogIn}
                        to={`/login?${params}`}
                        className="justify-center"
                    >
                        Log in
                    </Button>
                </div>
            </CenteredCard>
        );
    }

    const emailMatches = userEmail?.toLowerCase() === invitation.invitedEmail.toLowerCase();

    if (!emailMatches) {
        const handleLogout = () => {
            clearAuth();
            navigate(`/join/${token}`);
        };
        return (
            <CenteredCard
                title="Different account"
                subtitle={`You're signed in as ${userEmail}, but this invite is for ${invitation.invitedEmail}. Sign out and use the matching account.`}
                footer={
                    <button
                        onClick={handleLogout}
                        className="inline-flex items-center gap-1 text-muted-foreground hover:text-foreground"
                    >
                        <LogOut className="h-4 w-4"/> Sign out
                    </button>
                }
            />
        );
    }

    const handleAccept = async () => {
        if (!token) return;
        await acceptMutation.mutateAsync(token);
        navigate('/');
    };

    return (
        <CenteredCard title="Family invitation" subtitle={subtitle}>
            <Button
                variant="primary"
                icon={Check}
                onClick={handleAccept}
                disabled={acceptMutation.isPending}
                className="w-full justify-center"
            >
                {acceptMutation.isPending ? 'Joining...' : 'Accept invitation'}
            </Button>
        </CenteredCard>
    );
}
