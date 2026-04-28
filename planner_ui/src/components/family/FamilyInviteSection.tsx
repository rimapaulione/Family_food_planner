import {useState} from 'react';
import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {UserPlus, Send} from 'lucide-react';
import {SectionHeader} from '@/components/ui/SectionHeader';
import {Button} from '@/components/ui/Button';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {PendingInvitationCard} from '@/components/family/PendingInvitationCard';
import {
    useFamilyInvitations,
    useCreateInvitation,
    useCancelInvitation,
} from '@/hooks/useInvitations';
import {invitationCreateSchema, type InvitationCreateFormData} from '@/schemas/invitation';
import {inputClass} from '@/utils/inputClass';
import type {Invitation} from '@/types/invitation';

export function FamilyInviteSection() {
    const {data: invitations, isLoading, isError, error} = useFamilyInvitations();
    const createMutation = useCreateInvitation();
    const cancelMutation = useCancelInvitation();
    const [toCancel, setToCancel] = useState<Invitation | null>(null);

    const {register, handleSubmit, reset, formState: {errors}} = useForm<InvitationCreateFormData>({
        resolver: zodResolver(invitationCreateSchema),
        defaultValues: {email: ''},
    });

    const handleInvite = async (data: InvitationCreateFormData) => {
        await createMutation.mutateAsync(data);
        reset();
    };

    return (
        <section className="space-y-3">
            <SectionHeader icon={UserPlus} title="Invite Member"/>
            <form onSubmit={handleSubmit(handleInvite)} className="flex gap-2">
                <input
                    type="email"
                    placeholder="member@email.com"
                    {...register('email')}
                    className={inputClass(!!errors.email)}
                />
                <Button
                    variant="primary"
                    icon={Send}
                    type="submit"
                    disabled={createMutation.isPending}
                >
                    <span className="hidden sm:inline">
                        {createMutation.isPending ? 'Sending...' : 'Invite'}
                    </span>
                </Button>
            </form>
            {errors.email && (
                <p className="text-xs text-destructive">{errors.email.message}</p>
            )}
            <p className="text-xs text-muted-foreground">
                Copy the invite link and send it to the person. They click it to join.
            </p>

            {isLoading ? (
                <Spinner/>
            ) : isError ? (
                <ErrorMessage error={error} fallback="Failed to load invitations."/>
            ) : invitations && invitations.length > 0 ? (
                <div className="space-y-2">
                    <p className="text-xs font-medium text-muted-foreground">
                        Pending invitations:
                    </p>
                    {invitations.map((inv) => (
                        <PendingInvitationCard
                            key={inv.id}
                            invitation={inv}
                            onCancel={() => setToCancel(inv)}
                        />
                    ))}
                </div>
            ) : null}

            <ConfirmDialog
                open={toCancel !== null}
                onOpenChange={(open) => { if (!open) setToCancel(null); }}
                title="Cancel invitation?"
                description={toCancel ? `The invite for "${toCancel.invitedEmail}" will be cancelled.` : ''}
                confirmText="Cancel invitation"
                destructive
                onConfirm={() => {
                    if (toCancel) cancelMutation.mutate(toCancel.id);
                    setToCancel(null);
                }}
            />
        </section>
    );
}
