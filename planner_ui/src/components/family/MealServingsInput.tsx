import type {MealServings} from '@/types/family';

interface MealServingsInputProps {
    label: string;
    value: MealServings;
    onChange: (value: MealServings) => void;
}

const MEAL_KEYS = ['breakfast', 'lunch', 'dinner'] as const;
type MealKey = typeof MEAL_KEYS[number];

const MEAL_LABELS: Record<MealKey, string> = {
    breakfast: 'Breakfast',
    lunch: 'Lunch',
    dinner: 'Dinner',
};

const DEFAULT_SERVINGS = 4;

export function MealServingsInput({label, value, onChange}: MealServingsInputProps) {
    const update = (key: MealKey, newVal: number | null) => {
        onChange({...value, [key]: newVal});
    };

    return (
        <div>
            <label className="mb-1 block text-sm font-medium">{label}</label>
            <div className="space-y-2">
                {MEAL_KEYS.map((key) => {
                    const servings = value[key];
                    const active = servings !== null;
                    return (
                        <div key={key} className="flex items-center gap-3">
                            <button
                                type="button"
                                onClick={() => update(key, active ? null : DEFAULT_SERVINGS)}
                                className={`w-24 rounded-md px-3 py-1.5 text-xs font-medium ${
                                    active
                                        ? 'bg-primary text-primary-foreground'
                                        : 'border border-border text-muted-foreground'
                                }`}
                            >
                                {MEAL_LABELS[key]}
                            </button>
                            {active && (
                                <div className="flex items-center gap-1">
                                    <button
                                        type="button"
                                        onClick={() => update(key, Math.max(1, servings - 1))}
                                        className="rounded border border-border px-2 py-0.5 text-sm"
                                    >
                                        −
                                    </button>
                                    <span className="w-6 text-center text-sm font-medium">{servings}</span>
                                    <button
                                        type="button"
                                        onClick={() => update(key, Math.min(50, servings + 1))}
                                        className="rounded border border-border px-2 py-0.5 text-sm"
                                    >
                                        +
                                    </button>
                                    <span className="text-xs text-muted-foreground">servings</span>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
