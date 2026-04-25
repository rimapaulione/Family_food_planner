import {useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {ChefHat, LogOut, UserCircle, Menu} from 'lucide-react';
import {NavLink} from '@/components/layout/NavLink';
import {Button} from '@/components/ui/Button';
import {MobileDrawer} from '@/components/layout/MobileDrawer';
import {navItems} from '@/components/layout/navConfig';
import {useAuthStore} from '@/stores/useAuthStore';

export function Header() {
    const navigate = useNavigate();
    const clearAuth = useAuthStore((s) => s.clearAuth);
    const displayName = useAuthStore((s) => s.displayName);
    const avatarUrl = useAuthStore((s) => s.avatarUrl);
    const [drawerOpen, setDrawerOpen] = useState(false);

    const handleLogout = () => {
        clearAuth();
        navigate('/login');
    };

    return (
        <header className="border-b border-border bg-card">
            <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4 md:px-6 lg:px-8">
                <Link
                    to="/"
                    className="flex items-center gap-2 text-lg font-semibold text-foreground no-underline"
                >
                    <ChefHat className="h-5 w-5 text-primary"/>
                    Food Planner
                </Link>

                <nav className="hidden lg:flex items-center gap-4">
                    {navItems.map(({to, icon, label}) => (
                        <NavLink key={to} to={to} icon={icon} label={label}/>
                    ))}
                    <NavLink to="/profile" icon={UserCircle} label={displayName ?? ''} avatarUrl={avatarUrl}/>
                    <Button variant="outline" icon={LogOut} onClick={handleLogout}></Button>
                </nav>

                <button
                    type="button"
                    onClick={() => setDrawerOpen(true)}
                    aria-label="Open menu"
                    className="lg:hidden p-2 text-muted-foreground hover:text-foreground"
                >
                    <Menu className="h-6 w-6"/>
                </button>
            </div>

            <MobileDrawer
                open={drawerOpen}
                onClose={() => setDrawerOpen(false)}
                displayName={displayName}
                avatarUrl={avatarUrl}
                onLogout={handleLogout}
            />
        </header>
    );
}
