import {Link, useNavigate} from 'react-router-dom';
import {ChefHat, UtensilsCrossed, CalendarDays, ShoppingCart, ShoppingBasket, LogOut, UserCircle} from 'lucide-react';
import {NavLink} from '@/components/layout/NavLink';
import {Button} from '@/components/ui/Button';
import {useAuthStore} from '@/stores/useAuthStore';

export function Header() {
    const navigate = useNavigate();
    const clearAuth = useAuthStore((s) => s.clearAuth);
    const displayName = useAuthStore((s) => s.displayName);
    const avatarUrl: string | null = null;

    const handleLogout = () => {
        clearAuth();
        navigate('/login');
    };

    return (
        <header className="border-b border-border bg-card">
            <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
                <Link
                    to="/"
                    className="flex items-center gap-2 text-lg font-semibold text-foreground no-underline"
                >
                    <ChefHat className="h-5 w-5 text-primary"/>
                    Food Planner
                </Link>

                <nav className="flex items-center gap-4">
                    <NavLink to="/recipes" icon={UtensilsCrossed} label="Recipes"/>
                    <NavLink to="/planner" icon={CalendarDays} label="Planner"/>
                    <NavLink to="/shopping" icon={ShoppingCart} label="Shopping"/>
                    <NavLink to="/basics" icon={ShoppingBasket} label="Always Buy"/>
                    <NavLink to="/profile" icon={UserCircle} label={displayName ?? ''} avatarUrl={avatarUrl}/>
                    <Button variant="outline" icon={LogOut} onClick={handleLogout}></Button>
                </nav>
            </div>
        </header>
    );
}
