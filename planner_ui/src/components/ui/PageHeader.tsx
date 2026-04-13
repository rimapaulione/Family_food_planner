import {Link} from 'react-router-dom';
import {Plus, Search} from 'lucide-react';

interface PageHeaderProps {
    title: string;
    addLabel: string;
    /** Provide `addTo` for a Link, or `onAddClick` for a button. */
    addTo?: string;
    onAddClick?: () => void;
    search: string;
    onSearchChange: (value: string) => void;
    searchPlaceholder?: string;
}

const addButtonClass =
    'flex items-center gap-1 rounded-md bg-primary px-3 py-2 text-sm font-medium text-primary-foreground no-underline hover:opacity-90';

export function PageHeader({
    title,
    addLabel,
    addTo,
    onAddClick,
    search,
    onSearchChange,
    searchPlaceholder = 'Search...',
}: PageHeaderProps) {
    return (
        <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold text-foreground">{title}</h1>
                {addTo ? (
                    <Link to={addTo} className={addButtonClass}>
                        <Plus className="h-4 w-4"/> {addLabel}
                    </Link>
                ) : (
                    <button type="button" onClick={onAddClick} className={addButtonClass}>
                        <Plus className="h-4 w-4"/> {addLabel}
                    </button>
                )}
            </div>

            <div className="relative">
                <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground"/>
                <input
                    type="text"
                    placeholder={searchPlaceholder}
                    value={search}
                    onChange={(e) => onSearchChange(e.target.value)}
                    className="w-full rounded-md border border-input bg-background py-2 pl-9 pr-3 text-sm outline-none focus:ring-2 focus:ring-ring"
                />
            </div>
        </div>
    );
}
