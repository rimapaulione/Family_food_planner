interface TabButtonProps {
    label: string;
    count?: number;
    isActive: boolean;
    onClick: () => void;
}

export function TabButton({label, count, isActive, onClick}: TabButtonProps) {
    return (
        <button
            onClick={onClick}
            className={`pb-2 text-sm font-medium ${
                isActive
                    ? 'border-b-2 border-primary text-foreground'
                    : 'text-muted-foreground hover:text-foreground'
            }`}
        >
            {label}
            {count !== undefined && <span className="ml-1 text-xs">({count})</span>}
        </button>
    );
}
