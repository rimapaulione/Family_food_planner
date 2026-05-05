import {Pencil, User} from 'lucide-react';
import {Button} from '@/components/ui/Button';
import {Card} from '@/components/ui/Card';
import type {Family, MealServings} from '@/types/family';

interface FamilyDetailsViewProps {
    family: Family;
    canEdit: boolean;
    onEdit: () => void;
}

const MEAL_LABELS: Record<keyof MealServings, string> = {
    breakfast: 'Breakfast',
    lunch: 'Lunch',
    dinner: 'Dinner',
};

function formatDay(day: string) {
    return day.charAt(0) + day.slice(1).toLowerCase();
}

function MealRow({label, meals}: {label: string; meals: MealServings}) {
    const active = (Object.keys(MEAL_LABELS) as (keyof MealServings)[])
        .filter((k) => meals[k] !== null);
    return (
        <p className="flex flex-wrap items-center gap-x-2 text-muted-foreground">
            <span>{label}:</span>
            {active.length === 0 ? (
                <span>None</span>
            ) : (
                active.map((k, i) => (
                    <span key={k} className="inline-flex items-center gap-0.5">
                        {i > 0 && <span>,</span>}
                        {MEAL_LABELS[k]}
                        <User className="ml-0.5 h-3 w-3"/>
                        {meals[k]}
                    </span>
                ))
            )}
        </p>
    );
}

export function FamilyDetailsView({family, canEdit, onEdit}: FamilyDetailsViewProps) {
    return (
        <Card padding="md">
            <div className="space-y-1 text-sm">
                <div className="flex items-center justify-between gap-3">
                    <p className="font-medium text-foreground">{family.name}</p>
                    {canEdit && (
                        <Button variant="outline" icon={Pencil} onClick={onEdit}>
                            Edit
                        </Button>
                    )}
                </div>
                <p className="text-muted-foreground">
                    Shopping day: {formatDay(family.shoppingDay)}
                </p>
                <MealRow label="Weekday" meals={family.defaultWeekdayServings}/>
                <MealRow label="Weekend" meals={family.defaultWeekendServings}/>
                <p className="text-muted-foreground">
                    Don't repeat recipes within: {family.noRepeatRecipeDays} days
                </p>
            </div>
        </Card>
    );
}
