import {useState} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {ChefHat, Menu} from 'lucide-react';
import {Nav} from '@/components/layout/Nav';
import {MobileNav} from '@/components/layout/MobileNav';
import {useAuthStore} from '@/stores/useAuthStore';

export function Header() {
    const navigate = useNavigate();
    const clearAuth = useAuthStore((s) => s.clearAuth);
    const displayName = useAuthStore((s) => s.displayName);
    const avatarUrl = useAuthStore((s) => s.avatarUrl);
    const [mobileOpen, setMobileOpen] = useState(false);

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

                <Nav displayName={displayName} avatarUrl={avatarUrl} onLogout={handleLogout}/>

                <button
                    type="button"
                    onClick={() => setMobileOpen(true)}
                    aria-label="Open menu"
                    className="lg:hidden p-2 text-muted-foreground hover:text-foreground"
                >
                    <Menu className="h-6 w-6"/>
                </button>
            </div>

            <MobileNav
                open={mobileOpen}
                onClose={() => setMobileOpen(false)}
                displayName={displayName}
                avatarUrl={avatarUrl}
                onLogout={handleLogout}
            />
        </header>
    );
}
