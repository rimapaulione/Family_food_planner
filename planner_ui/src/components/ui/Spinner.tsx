import {Loader2} from 'lucide-react';

interface SpinnerProps {
    text?: string;
}

export function Spinner({text = 'Loading...'}: SpinnerProps) {
    return (
        <div className="flex items-center gap-2 text-muted-foreground">
            <Loader2 className="h-4 w-4 animate-spin"/>
            <span className="text-sm">{text}</span>
        </div>
    );
}
