interface ProgressBarProps {
    value: number;
    max: number;
    label?: string;
}

export function ProgressBar({value, max, label}: ProgressBarProps) {
    const percent = max > 0 ? Math.round((value / max) * 100) : 0;

    return (
        <div className="space-y-1">
            {label && (
                <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span>{label}</span>
                    <span className="tabular-nums">{percent}%</span>
                </div>
            )}
            <div
                role="progressbar"
                aria-valuenow={value}
                aria-valuemin={0}
                aria-valuemax={max}
                className="h-2 w-full overflow-hidden rounded-full bg-muted"
            >
                <div
                    className="h-full bg-success transition-all duration-300"
                    style={{width: `${percent}%`}}
                />
            </div>
        </div>
    );
}
