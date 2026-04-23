import {Link} from 'react-router-dom';
import type {LucideIcon} from 'lucide-react';

interface ShortcutCardProps {
    to: string;
    icon: LucideIcon;
    title: string;
    description: string;
}

export function ShortcutCard({to, icon: Icon, title, description}: ShortcutCardProps) {
    return (
        <Link
            to={to}
            className="flex items-center gap-3 rounded-lg border border-border bg-card p-4 no-underline transition hover:border-primary"
        >
            <Icon className="h-8 w-8 text-primary"/>
            <div>
                <h2 className="font-semibold text-card-foreground">{title}</h2>
                <p className="text-sm text-muted-foreground">{description}</p>
            </div>
        </Link>
    );
}
