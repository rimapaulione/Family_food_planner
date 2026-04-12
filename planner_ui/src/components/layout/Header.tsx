import {Link} from 'react-router-dom';
import {ChefHat, UtensilsCrossed, CalendarDays, ShoppingCart, ShoppingBasket} from 'lucide-react';
import {NavLink} from '@/components/ui/NavLink';

export function Header() {
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
                </nav>
            </div>
        </header>
    );
}
