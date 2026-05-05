import {useShoppingList, useToggleBought} from '@/hooks/useShoppingList';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {NotFound} from '@/components/ui/NotFound';
import {ProgressBar} from '@/components/ui/ProgressBar';
import {ShoppingItemRow} from './ShoppingItemRow';
import {ManualItemRow} from './ManualItemRow';
import {CollapsibleCard} from './CollapsibleCard';

interface ShoppingListViewProps {
    weekStart: string;
}

export function ShoppingListView({weekStart}: ShoppingListViewProps) {
    const {data, isLoading, isError, error} = useShoppingList(weekStart);
    const toggleMutation = useToggleBought(weekStart);

    if (isLoading) return <Spinner/>;
    if (isError) return <ErrorMessage error={error} fallback="Failed to load shopping list."/>;
    if (!data || (data.items.length === 0 && data.manualItems.length === 0)) {
        return <NotFound message="No items. Plan some meals to generate a shopping list."/>;
    }

    const recipeItems = data.items.filter((i) => !i.isBought);
    const boughtRecipeItems = data.items.filter((i) => i.isBought);
    const manualItems = data.manualItems.filter((i) => !i.isBought);
    const boughtManualItems = data.manualItems.filter((i) => i.isBought);

    const total = data.items.length + data.manualItems.length;
    const bought = boughtRecipeItems.length + boughtManualItems.length;

    return (
        <div className="space-y-3">
            <ProgressBar value={bought} max={total} label={`${bought} of ${total} bought`}/>

            {recipeItems.length > 0 && (
                <CollapsibleCard title="Recipe items" count={recipeItems.length} defaultOpen>
                    {recipeItems.map((item) => (
                        <ShoppingItemRow
                            key={`${item.ingredientId}-${item.isBought}`}
                            item={item}
                            onToggle={() => toggleMutation.mutate(item)}
                        />
                    ))}
                </CollapsibleCard>
            )}

            <CollapsibleCard title="Extra items" count={manualItems.length} defaultOpen>
                {manualItems.length === 0 ? (
                    <p className="px-2 py-1.5 text-sm text-muted-foreground">No extra items.</p>
                ) : (
                    manualItems.map((item) => <ManualItemRow key={item.id} item={item}/>)
                )}
            </CollapsibleCard>

            {bought > 0 && (
                <CollapsibleCard title="Bought" count={bought} defaultOpen={false}>
                    {boughtRecipeItems.map((item) => (
                        <ShoppingItemRow
                            key={`${item.ingredientId}-${item.isBought}`}
                            item={item}
                            onToggle={() => toggleMutation.mutate(item)}
                        />
                    ))}
                    {boughtManualItems.map((item) => (
                        <ManualItemRow key={item.id} item={item}/>
                    ))}
                </CollapsibleCard>
            )}
        </div>
    );
}
