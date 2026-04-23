import type {ReactNode} from 'react';
import {ChefHat} from 'lucide-react';

interface AuthCardProps {
    title: string;
    subtitle: string;
    children: ReactNode;
}

export function AuthCard({title, subtitle, children}: AuthCardProps) {
    return (
        <div className="flex min-h-screen items-center justify-center bg-background px-4">
            <div className="w-full max-w-sm space-y-6">
                <div className="text-center">
                    <ChefHat className="mx-auto h-10 w-10 text-primary"/>
                    <h1 className="mt-2 text-2xl font-bold text-foreground">{title}</h1>
                    <p className="text-sm text-muted-foreground">{subtitle}</p>
                </div>
                {children}
            </div>
        </div>
    );
}
