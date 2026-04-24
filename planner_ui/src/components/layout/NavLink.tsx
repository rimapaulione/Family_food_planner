import {Link, useLocation} from 'react-router-dom';
import type {LucideIcon} from 'lucide-react';

interface NavLinkProps {
    to: string;
    icon: LucideIcon;
    label: string;
    avatarUrl?: string | null;
}

export function NavLink({to, icon: Icon, label, avatarUrl}: NavLinkProps) {
    const {pathname} = useLocation();
    const isActive = pathname.startsWith(to);

    return (
        <Link
            to={to}
            className={`flex items-center gap-1 text-sm no-underline ${
                isActive
                    ? 'text-primary font-medium'
                    : 'text-muted-foreground hover:text-foreground'
            }`}
        >
            {avatarUrl ? (
                <img src={avatarUrl} alt="" className="h-5 w-5 rounded-full object-cover"/>
            ) : (
                <Icon className="h-4 w-4"/>
            )}
            {label}
        </Link>
    );
}
