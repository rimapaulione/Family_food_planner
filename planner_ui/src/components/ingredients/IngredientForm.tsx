import {useRef, useState} from 'react';
import {Check, X} from 'lucide-react';
import {UNITS} from '@/constants/units';

interface IngredientFormProps {
    initialName?: string;
    initialUnit?: string;
    onSave: (data: {nameLt: string; unit: string}) => void;
    onCancel: () => void;
}

export function IngredientForm({
    initialName = '',
    initialUnit = 'g',
    onSave,
    onCancel,
}: IngredientFormProps) {
    const [name, setName] = useState(initialName);
    const [unit, setUnit] = useState(initialUnit);
    const unitRef = useRef<HTMLSelectElement>(null);

    const handleSave = () => {
        if (!name.trim()) return;
        onSave({nameLt: name.trim(), unit});
    };

    const handleNameKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            if (name.trim()) unitRef.current?.focus();
        } else if (e.key === 'Escape') {
            e.preventDefault();
            onCancel();
        }
    };

    const handleUnitKeyDown = (e: React.KeyboardEvent<HTMLSelectElement>) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleSave();
        } else if (e.key === 'Escape') {
            e.preventDefault();
            onCancel();
        }
    };

    return (
        <div className="flex items-center gap-2 rounded-md border border-dashed border-primary px-3 py-2">
            <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                onKeyDown={handleNameKeyDown}
                autoFocus
                placeholder="Ingredient name..."
                className="flex-1 rounded-md border border-input bg-background px-2 py-1 text-sm outline-none focus:ring-2 focus:ring-ring"
            />
            <select
                ref={unitRef}
                value={unit}
                onChange={(e) => setUnit(e.target.value)}
                onKeyDown={handleUnitKeyDown}
                className="w-16 rounded-md border border-input bg-background px-1 py-1 text-sm"
            >
                {UNITS.map((u) => (
                    <option key={u} value={u}>{u}</option>
                ))}
            </select>
            <button
                onClick={handleSave}
                className="rounded p-1 text-green-600 hover:bg-green-50 dark:hover:bg-green-900/30"
            >
                <Check className="h-4 w-4"/>
            </button>
            <button
                onClick={onCancel}
                className="rounded p-1 text-muted-foreground hover:bg-accent"
            >
                <X className="h-4 w-4"/>
            </button>
        </div>
    );
}
