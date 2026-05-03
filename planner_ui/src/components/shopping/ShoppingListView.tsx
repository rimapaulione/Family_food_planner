import {useShoppingList, useToggleBought} from '@/hooks/useShoppingList';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {NotFound} from '@/components/ui/NotFound';
import {ProgressBar} from '@/components/ui/ProgressBar';
import {ShoppingItemRow} from './ShoppingItemRow';

interface ShoppingListViewProps {
    weekStart: string;
}

export function ShoppingListView({weekStart}: ShoppingListViewProps) {
    const {data, isLoading, isError, error} = useShoppingList(weekStart);
    const toggleMutation = useToggleBought(weekStart);

    if (isLoading) return <Spinner/>;
    if (isError) return <ErrorMessage error={error} fallback="Failed to load shopping list."/>;
    if (!data || data.items.length === 0) {
        return <NotFound message="No items. Plan some meals to generate a shopping list."/>;
    }

    const total = data.items.length;
    const bought = data.items.filter((i) => i.isBought).length;
    const sortedItems = [...data.items].sort(
        (a, b) => Number(a.isBought) - Number(b.isBought),
    );

    return (
        <div className="space-y-3">
            <ProgressBar value={bought} max={total} label={`${bought} of ${total} bought`}/>
            <div className="space-y-1">
                {sortedItems.map((item) => (
                    <ShoppingItemRow
                        key={`${item.ingredientId}-${item.isBought}`}
                        item={item}
                        onToggle={() => toggleMutation.mutate(item)}
                    />
                ))}
            </div>
        </div>
    );
}
