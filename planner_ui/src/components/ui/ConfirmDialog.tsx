import * as AlertDialog from '@radix-ui/react-alert-dialog';
import type {ReactNode} from 'react';

interface ConfirmDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    title: string;
    description?: ReactNode;
    confirmText?: string;
    cancelText?: string;
    destructive?: boolean;
    onConfirm: () => void;
}

export function ConfirmDialog({
    open,
    onOpenChange,
    title,
    description,
    confirmText = 'Confirm',
    cancelText = 'Cancel',
    destructive = false,
    onConfirm,
}: ConfirmDialogProps) {
    return (
        <AlertDialog.Root open={open} onOpenChange={onOpenChange}>
            <AlertDialog.Portal>
                <AlertDialog.Overlay className="fixed inset-0 z-50 bg-black/50 data-[state=open]:animate-in data-[state=open]:fade-in"/>
                <AlertDialog.Content className="fixed left-1/2 top-1/2 z-50 w-[90vw] max-w-md -translate-x-1/2 -translate-y-1/2 rounded-lg border border-border bg-card p-6 shadow-lg">
                    <AlertDialog.Title className="text-lg font-semibold text-foreground">
                        {title}
                    </AlertDialog.Title>
                    {description && (
                        <AlertDialog.Description className="mt-2 text-sm text-muted-foreground">
                            {description}
                        </AlertDialog.Description>
                    )}
                    <div className="mt-6 flex justify-end gap-2">
                        <AlertDialog.Cancel className="rounded-md border border-border bg-background px-4 py-2 text-sm hover:bg-secondary">
                            {cancelText}
                        </AlertDialog.Cancel>
                        <AlertDialog.Action
                            onClick={onConfirm}
                            className={
                                destructive
                                    ? 'rounded-md bg-destructive px-4 py-2 text-sm text-destructive-foreground hover:opacity-90'
                                    : 'rounded-md bg-primary px-4 py-2 text-sm text-primary-foreground hover:opacity-90'
                            }
                        >
                            {confirmText}
                        </AlertDialog.Action>
                    </div>
                </AlertDialog.Content>
            </AlertDialog.Portal>
        </AlertDialog.Root>
    );
}
