import {Plus, UtensilsCrossed} from 'lucide-react';
import {useAuthStore} from '@/stores/useAuthStore';
import {ShortcutCard} from '@/components/ui/ShortcutCard';
import {capitalize} from '@/utils/capitalize';

export function HomePage() {
    const displayName = useAuthStore((s) => s.displayName);

    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-2xl font-bold text-foreground">
                    Hello, {capitalize(displayName)}!
                </h1>
                <p className="text-muted-foreground">
                    Welcome to your family food planner.
                </p>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
                <ShortcutCard
                    to="/recipes"
                    icon={UtensilsCrossed}
                    title="My Recipes"
                    description="Browse and manage recipes"
                />
                <ShortcutCard
                    to="/recipes/new"
                    icon={Plus}
                    title="New Recipe"
                    description="Add a new recipe"
                />
            </div>
        </div>
    );
}
