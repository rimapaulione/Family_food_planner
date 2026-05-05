import {useState, type ReactNode} from 'react';
import {ChevronDown, ChevronRight} from 'lucide-react';
import {Card} from '@/components/ui/Card';

interface CollapsibleCardProps {
    title: string;
    count?: number;
    defaultOpen?: boolean;
    children: ReactNode;
}

export function CollapsibleCard({title, count, defaultOpen = true, children}: CollapsibleCardProps) {
    const [open, setOpen] = useState(defaultOpen);

    return (
        <Card padding="sm" className="space-y-1">
            <button
                type="button"
                onClick={() => setOpen((v) => !v)}
                className="flex w-full items-center justify-between rounded-md px-2 py-1.5 text-left text-sm font-medium hover:bg-accent"
                aria-expanded={open}
            >
                <span className="flex items-center gap-2">
                    {open ? <ChevronDown className="h-4 w-4"/> : <ChevronRight className="h-4 w-4"/>}
                    {title}
                    {count !== undefined && count > 0 && (
                        <span className="text-xs text-muted-foreground">({count})</span>
                    )}
                </span>
            </button>
            {open && <div className="space-y-1 px-1 pb-1">{children}</div>}
        </Card>
    );
}
