import {Lock, Unlock} from 'lucide-react';
import {Button} from '@/components/ui/Button';
import {MealPlanGrid} from './MealPlanGrid';
import {useUpdateMealPlan} from '@/hooks/useMealPlan';
import {useAuthStore} from '@/stores/useAuthStore';
import {ROLE} from '@/types/auth';
import type {Family} from '@/types/family';
import type {MealPlan} from '@/types/mealPlan';
import {PLAN_STATUS} from '@/types/mealPlan';

interface PlannerWeekSectionProps {
    plan: MealPlan;
    family: Family;
    onSlotClick: (slotId: string) => void;
    onSlotRemove: (slotId: string) => void;
}

export function PlannerWeekSection({plan, family, onSlotClick, onSlotRemove}: PlannerWeekSectionProps) {
    const isAdmin = useAuthStore((s) => s.role === ROLE.ADMIN);
    const updatePlanMutation = useUpdateMealPlan();
    const locked = plan.status === PLAN_STATUS.LOCKED;

    const toggleLock = () => {
        const nextStatus = locked ? PLAN_STATUS.DRAFT : PLAN_STATUS.LOCKED;
        updatePlanMutation.mutate({planId: plan.id, request: {status: nextStatus}});
    };

    return (
        <section className="space-y-3">
            <div className="flex items-center justify-between gap-2">
                <h2 className="flex items-center gap-2 text-lg font-semibold">
                    Week {plan.startDate} – {plan.endDate}
                    {locked && (
                        <span className="rounded-full bg-success/15 px-2 py-0.5 text-xs font-normal text-success">
                            Confirmed
                        </span>
                    )}
                </h2>
                {isAdmin && (
                    <Button
                        variant={locked ? 'outline' : 'success'}
                        icon={locked ? Unlock : Lock}
                        onClick={toggleLock}
                        disabled={updatePlanMutation.isPending}
                    >
                        {locked ? 'Unlock plan' : 'Confirm & lock'}
                    </Button>
                )}
            </div>
            <MealPlanGrid
                plan={plan}
                family={family}
                onSlotClick={locked ? undefined : onSlotClick}
                onSlotRemove={locked ? undefined : onSlotRemove}
                disabled={locked}
            />
        </section>
    );
}
