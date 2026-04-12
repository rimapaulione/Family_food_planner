import { Link } from 'react-router-dom';
import { ChefHat, UtensilsCrossed, CalendarDays, ShoppingCart, ShoppingBasket } from 'lucide-react';

export function Header() {
  return (
    <header className="border-b border-border bg-card">
      <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-4">
        <Link
          to="/"
          className="flex items-center gap-2 text-lg font-semibold text-foreground no-underline"
        >
          <ChefHat className="h-5 w-5 text-primary" />
          Food Planner
        </Link>

        <nav className="flex items-center gap-4">
          <Link
            to="/recipes"
            className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground no-underline"
          >
            <UtensilsCrossed className="h-4 w-4" />
            Recipes
          </Link>

          <Link
            to="/planner"
            className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground no-underline"
          >
            <CalendarDays className="h-4 w-4" />
            Planner
          </Link>

          <Link
            to="/shopping"
            className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground no-underline"
          >
            <ShoppingCart className="h-4 w-4" />
            Shopping
          </Link>

          <Link
            to="/basics"
            className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground no-underline"
          >
            <ShoppingBasket className="h-4 w-4" />
            Always Buy
          </Link>
        </nav>
      </div>
    </header>
  );
}
