import {useEffect} from 'react';
import {Link, useLocation} from 'react-router-dom';
import {X, UserCircle, LogOut} from 'lucide-react';
import {navItems} from '@/components/layout/navConfig';

interface MobileDrawerProps {
    open: boolean;
    onClose: () => void;
    displayName: string | null;
    avatarUrl: string | null;
    onLogout: () => void;
}

export function MobileDrawer({open, onClose, displayName, avatarUrl, onLogout}: MobileDrawerProps) {
    const {pathname} = useLocation();

    useEffect(() => {
        if (!open) return;
        const onKey = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose();
        };
        window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    }, [open, onClose]);

    const itemClass = (active: boolean) =>
        `flex items-center gap-3 rounded-md px-3 py-3 text-base no-underline ${
            active
                ? 'bg-accent text-primary font-medium'
                : 'text-muted-foreground hover:bg-accent hover:text-foreground'
        }`;

    return (
        <>
            <div
                className={`fixed inset-0 z-40 bg-black/40 transition-opacity lg:hidden ${
                    open ? 'opacity-100' : 'pointer-events-none opacity-0'
                }`}
                onClick={onClose}
                aria-hidden="true"
            />
            <aside
                className={`fixed inset-y-0 right-0 z-50 w-64 bg-card border-l border-border transition-transform lg:hidden ${
                    open ? 'translate-x-0' : 'translate-x-full'
                }`}
                role="dialog"
                aria-modal="true"
            >
                <div className="flex items-center justify-end p-3">
                    <button
                        type="button"
                        onClick={onClose}
                        aria-label="Close menu"
                        className="p-2 text-muted-foreground hover:text-foreground"
                    >
                        <X className="h-5 w-5"/>
                    </button>
                </div>

                <nav className="flex flex-col gap-1 px-2">
                    {navItems.map(({to, icon: Icon, label}) => (
                        <Link
                            key={to}
                            to={to}
                            onClick={onClose}
                            className={itemClass(pathname.startsWith(to))}
                        >
                            <Icon className="h-5 w-5"/>
                            {label}
                        </Link>
                    ))}

                    <Link
                        to="/profile"
                        onClick={onClose}
                        className={itemClass(pathname.startsWith('/profile'))}
                    >
                        {avatarUrl ? (
                            <img src={avatarUrl} alt="" className="h-7 w-7 rounded-full object-cover"/>
                        ) : (
                            <UserCircle className="h-5 w-5"/>
                        )}
                        {displayName ?? 'Profile'}
                    </Link>

                    <button
                        type="button"
                        onClick={() => {
                            onClose();
                            onLogout();
                        }}
                        className="flex items-center gap-3 rounded-md px-3 py-3 text-base text-muted-foreground hover:bg-accent hover:text-foreground"
                    >
                        <LogOut className="h-5 w-5"/>
                        Logout
                    </button>
                </nav>
            </aside>
        </>
    );
}
