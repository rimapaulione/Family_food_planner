import type {ReactNode} from 'react';

interface AuthCardProps {
    title: string;
    children: ReactNode;
}

export function AuthCard({title, children}: AuthCardProps) {
    return (
        <div className="flex min-h-screen items-center justify-center bg-background p-4">
            <div className="w-full max-w-md space-y-6 rounded-lg border border-border bg-card p-6 shadow-sm">
                <h1 className="text-center text-2xl font-bold text-foreground">{title}</h1>
                {children}
            </div>
        </div>
    );
}
