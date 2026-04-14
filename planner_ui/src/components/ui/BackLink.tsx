import {Link} from 'react-router-dom';
import {ArrowLeft} from 'lucide-react';

interface BackLinkProps {
    to: string;
    label?: string;
}

export function BackLink({to, label = 'Back'}: BackLinkProps) {
    return (
        <Link to={to} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
            <ArrowLeft className="h-4 w-4"/> {label}
        </Link>
    );
}
