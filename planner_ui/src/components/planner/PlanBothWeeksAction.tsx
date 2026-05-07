import {useState} from 'react';
import {Wand2} from 'lucide-react';
import {useAutoFillPlan} from '@/hooks/useMealPlan';
import {Button} from '@/components/ui/Button';
import {ConfirmDialog} from '@/components/ui/ConfirmDialog';
import type {MealPlan} from '@/types/mealPlan';

interface PlanBothWeeksActionProps {
    currentWeek: MealPlan;
    nextWeek: MealPlan;
}

export function PlanBothWeeksAction({currentWeek, nextWeek}: PlanBothWeeksActionProps) {
    const [open, setOpen] = useState(false);
    const autoFillMutation = useAutoFillPlan();

    const planBoth = async () => {
        await autoFillMutation.mutateAsync(currentWeek.id);
        await autoFillMutation.mutateAsync(nextWeek.id);
        setOpen(false);
    };

    return (
        <>
            <Button
                variant="outline"
                icon={Wand2}
                onClick={() => setOpen(true)}
                disabled={autoFillMutation.isPending}
                className="hidden md:flex"
            >
                Plan both weeks
            </Button>
            <ConfirmDialog
                open={open}
                onOpenChange={setOpen}
                title="Plan both weeks?"
                description={`This will fill empty slots for the weeks of ${currentWeek.startDate} and ${nextWeek.startDate}.`}
                confirmText="Plan"
                onConfirm={planBoth}
            />
        </>
    );
}
