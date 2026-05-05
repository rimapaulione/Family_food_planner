import {useShoppingList, useToggleBought, useToggleManualBought} from '@/hooks/useShoppingList';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {ProgressBar} from '@/components/ui/ProgressBar';
import {ShoppingItemRow} from './ShoppingItemRow';
import {ManualItemRow} from './ManualItemRow';
import {CollapsibleCard} from './CollapsibleCard';
import {AddManualItemForm} from './AddManualItemForm';

interface ShoppingListViewProps {
    weekStart: string;
}

export function ShoppingListView({weekStart}: ShoppingListViewProps) {
    const {data, isLoading, isError, error} = useShoppingList(weekStart);
    const toggleMutation = useToggleBought(weekStart);
    const toggleManualMutation = useToggleManualBought(weekStart);

    if (isLoading) return <Spinner/>;
    if (isError) return <ErrorMessage error={error} fallback="Failed to load shopping list."/>;
    if (!data) return null;

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
                {manualItems.map((item) => (
                    <ManualItemRow
                        key={item.id}
                        item={item}
                        onToggle={() => toggleManualMutation.mutate(item)}
                    />
                ))}
                <AddManualItemForm weekStart={weekStart}/>
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
                        <ManualItemRow
                            key={item.id}
                            item={item}
                            onToggle={() => toggleManualMutation.mutate(item)}
                        />
                    ))}
                </CollapsibleCard>
            )}
        </div>
    );
}
