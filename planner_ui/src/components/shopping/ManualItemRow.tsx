import type {ManualShoppingItem} from '@/types/shoppingList';

interface ManualItemRowProps {
    item: ManualShoppingItem;
    onToggle?: () => void;
}

export function ManualItemRow({item, onToggle}: ManualItemRowProps) {
    const interactive = onToggle !== undefined;
    const className = `flex items-center gap-3 rounded-md px-2 py-1.5 ${
        interactive ? 'cursor-pointer hover:bg-accent' : ''
    } ${item.isBought ? 'bg-muted/30' : ''}`;

    const content = (
        <>
            <input
                type="checkbox"
                checked={item.isBought}
                onChange={onToggle}
                disabled={!interactive}
                className="h-4 w-4 cursor-pointer accent-success disabled:cursor-default"
                aria-label={`${item.name} bought`}
            />
            <span
                className={`flex-1 text-sm ${
                    item.isBought ? 'text-muted-foreground line-through' : 'text-foreground'
                }`}
            >
                {item.name}
            </span>
            <span
                className={`text-sm tabular-nums ${
                    item.isBought ? 'text-muted-foreground' : 'text-foreground'
                }`}
            >
                {item.quantity} {item.unit}
            </span>
        </>
    );

    return interactive ? (
        <label className={className}>{content}</label>
    ) : (
        <div className={className}>{content}</div>
    );
}
