import type {ReactNode} from 'react';
import type {LucideIcon} from 'lucide-react';
import {ChefHat} from 'lucide-react';

interface CenteredCardProps {
    title: string;
    subtitle?: string;
    icon?: LucideIcon;
    children?: ReactNode;
    footer?: ReactNode;
    maxWidth?: 'sm' | 'md';
}

export function CenteredCard({
    title,
    subtitle,
    icon: Icon = ChefHat,
    children,
    footer,
    maxWidth = 'sm',
}: CenteredCardProps) {
    const widthClass = maxWidth === 'md' ? 'max-w-md' : 'max-w-sm';
    return (
        <div className="flex min-h-dvh items-center justify-center bg-background px-4 py-8">
            <div className={`w-full ${widthClass} space-y-6`}>
                <div className="text-center">
                    <Icon className="mx-auto h-10 w-10 text-primary"/>
                    <h1 className="mt-2 text-2xl font-bold text-foreground">{title}</h1>
                    {subtitle && <p className="text-sm text-muted-foreground">{subtitle}</p>}
                </div>
                {children}
                {footer && <div className="text-center text-sm">{footer}</div>}
            </div>
        </div>
    );
}
