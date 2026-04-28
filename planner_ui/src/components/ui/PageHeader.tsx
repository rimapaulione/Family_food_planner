import {Plus, Search} from 'lucide-react';
import {Button} from '@/components/ui/Button';

interface PageHeaderProps {
    title: string;
    addLabel: string;
    addLabelExtra?: string;
    /** Provide `addTo` for a Link, or `onAddClick` for a button. */
    addTo?: string;
    onAddClick?: () => void;
    search: string;
    onSearchChange: (value: string) => void;
    searchPlaceholder?: string;
}

export function PageHeader({
    title,
    addLabel,
    addLabelExtra,
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
                <Button variant="primary" icon={Plus} to={addTo} onClick={onAddClick}>
                    {addLabel}
                    {addLabelExtra && <span className="hidden md:inline">&nbsp;{addLabelExtra}</span>}
                </Button>
            </div>

            <div className="relative">
                <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground"/>
                <input
                    type="text"
                    placeholder={searchPlaceholder}
                    value={search}
                    onChange={(e) => onSearchChange(e.target.value)}
                    className="w-full rounded-md border border-input bg-background py-2 pl-9 pr-3 text-base md:text-sm outline-none focus:ring-2 focus:ring-ring"
                />
            </div>
        </div>
    );
}
