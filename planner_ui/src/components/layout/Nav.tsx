import {LogOut, UserCircle} from 'lucide-react';
import {NavLink} from '@/components/layout/NavLink';
import {Button} from '@/components/ui/Button';
import {navItems} from '@/components/layout/navConfig';

interface NavProps {
    displayName: string | null;
    avatarUrl: string | null;
    onLogout: () => void;
}

export function Nav({displayName, avatarUrl, onLogout}: NavProps) {
    return (
        <nav className="hidden lg:flex items-center gap-4">
            {navItems.map(({to, icon, label}) => (
                <NavLink key={to} to={to} icon={icon} label={label}/>
            ))}
            <NavLink to="/profile" icon={UserCircle} label={displayName ?? ''} avatarUrl={avatarUrl}/>
            <Button variant="outline" icon={LogOut} onClick={onLogout}></Button>
        </nav>
    );
}
