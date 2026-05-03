import type {ShoppingItem} from '@/types/shoppingList';

interface ShoppingItemRowProps {
    item: ShoppingItem;
    onToggle: () => void;
}

export function ShoppingItemRow({item, onToggle}: ShoppingItemRowProps) {
    return (
        <label
            className={`flex cursor-pointer items-center gap-3 rounded-md px-2 py-1.5 hover:bg-accent ${
                item.isBought ? 'bg-muted/30' : ''
            }`}
        >
            <input
                type="checkbox"
                checked={item.isBought}
                onChange={onToggle}
                className="h-4 w-4 cursor-pointer accent-primary"
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
        </label>
    );
}
