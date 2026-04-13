import {Link} from 'react-router-dom';
import {Plus, Search} from 'lucide-react';

interface PageHeaderProps {
    title: string;
    addLabel: string;
    addTo: string;
    search: string;
    onSearchChange: (value: string) => void;
    searchPlaceholder?: string;
}

export function PageHeader({
    title,
    addLabel,
    addTo,
    search,
    onSearchChange,
    searchPlaceholder = 'Search...',
}: PageHeaderProps) {
    return (
        <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold text-[hsl(var(--foreground))]">{title}</h1>
                <Link
                    to={addTo}
                    className="flex items-center gap-1 rounded-md bg-[hsl(var(--primary))] px-3 py-2 text-sm font-medium text-[hsl(var(--primary-foreground))] no-underline hover:opacity-90"
                >
                    <Plus className="h-4 w-4"/> {addLabel}
                </Link>
            </div>

            <div className="relative">
                <Search className="absolute left-3 top-2.5 h-4 w-4 text-[hsl(var(--muted-foreground))]"/>
                <input
                    type="text"
                    placeholder={searchPlaceholder}
                    value={search}
                    onChange={(e) => onSearchChange(e.target.value)}
                    className="w-full rounded-md border border-[hsl(var(--input))] bg-[hsl(var(--background))] py-2 pl-9 pr-3 text-sm outline-none focus:ring-2 focus:ring-[hsl(var(--ring))]"
                />
            </div>
        </div>
    );
}
