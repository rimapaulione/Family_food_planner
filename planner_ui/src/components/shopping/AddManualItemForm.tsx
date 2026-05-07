import {useState, type FormEvent} from 'react';
import {Plus} from 'lucide-react';
import {useAddManualItem} from '@/hooks/useShoppingList';

interface AddManualItemFormProps {
    weekStart: string;
}

export function AddManualItemForm({weekStart}: AddManualItemFormProps) {
    const [name, setName] = useState('');
    const addMutation = useAddManualItem(weekStart);

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault();
        const trimmed = name.trim();
        if (!trimmed) return;
        addMutation.mutate(trimmed, {
            onSuccess: () => setName(''),
        });
    };

    return (
        <form onSubmit={handleSubmit} className="flex gap-2 px-1 py-1">
            <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Add item (e.g., Paper towels)"
                maxLength={100}
                className="min-w-0 flex-1 rounded-md border border-input bg-background px-3 py-1.5 text-base md:text-sm outline-none focus:ring-2 focus:ring-ring"
            />
            <button
                type="submit"
                disabled={addMutation.isPending || name.trim().length === 0}
                className="flex items-center gap-1 rounded-md bg-secondary px-3 py-1.5 text-sm hover:bg-accent disabled:opacity-50"
            >
                <Plus className="h-3.5 w-3.5"/> Add
            </button>
        </form>
    );
}
