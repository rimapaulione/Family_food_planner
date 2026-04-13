import type {LucideIcon} from 'lucide-react';
import type {ReactNode} from 'react';

interface IconStatProps {
    icon: LucideIcon;
    children: ReactNode;
    className?: string;
}

export function IconStat({icon: Icon, children, className = ''}: IconStatProps) {
    return (
        <span className={`flex items-center gap-0.5 ${className}`}>
            <Icon className="h-3 w-3"/>
            {children}
        </span>
    );
}
