import {useMemo, useState} from 'react';
import {CalendarDays} from 'lucide-react';
import {useFamily} from '@/hooks/useFamily';
import {useMealPlanCurrentAndNext, useUpdateMealSlot} from '@/hooks/useMealPlan';
import {useAuthStore} from '@/stores/useAuthStore';
import {ROLE} from '@/types/auth';
import {PLAN_STATUS} from '@/types/mealPlan';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {SectionHeader} from '@/components/ui/SectionHeader';
import {PlannerWeekSection} from '@/components/planner/PlannerWeekSection';
import {PlanBothWeeksAction} from '@/components/planner/PlanBothWeeksAction';
import {MealPlanMobileView} from '@/components/planner/MealPlanMobileView';
import {MealSlotEditModal} from '@/components/planner/MealSlotEditModal';

export function PlannerPage() {
    const {data, isLoading, isError, error} = useMealPlanCurrentAndNext();
    const {data: family} = useFamily();
    const isAdmin = useAuthStore((s) => s.role === ROLE.ADMIN);
    const [editingSlotId, setEditingSlotId] = useState<string | null>(null);
    const updateSlotMutation = useUpdateMealSlot();

    const handleRemove = (slotId: string) => {
        updateSlotMutation.mutate({slotId, request: {recipeId: null, servings: null}});
    };

    const plannedRecipeIds = useMemo(() => {
        const ids = new Set<string>();
        if (!data) return ids;
        for (const s of data.currentWeek.slots) if (s.recipeId) ids.add(s.recipeId);
        for (const s of data.nextWeek.slots) if (s.recipeId) ids.add(s.recipeId);
        return ids;
    }, [data]);

    if (isLoading || !family) return <Spinner/>;
    if (isError) return <ErrorMessage error={error} fallback="Failed to load meal plan."/>;
    if (!data) return null;

    const allSlots = [...data.currentWeek.slots, ...data.nextWeek.slots];
    const editingSlot = allSlots.find((s) => s.id === editingSlotId) ?? null;
    const bothDraft = data.currentWeek.status === PLAN_STATUS.DRAFT
        && data.nextWeek.status === PLAN_STATUS.DRAFT;

    return (
        <div className="mx-auto max-w-6xl space-y-8">
            <div className="flex items-center justify-between gap-2">
                <SectionHeader icon={CalendarDays} title="Meal Plan" level="h1"/>
                {isAdmin && bothDraft && (
                    <PlanBothWeeksAction
                        currentWeek={data.currentWeek}
                        nextWeek={data.nextWeek}
                    />
                )}
            </div>

            <div className="hidden space-y-8 md:block">
                <PlannerWeekSection
                    plan={data.currentWeek}
                    family={family}
                    onSlotClick={setEditingSlotId}
                    onSlotRemove={handleRemove}
                />
                <PlannerWeekSection
                    plan={data.nextWeek}
                    family={family}
                    onSlotClick={setEditingSlotId}
                    onSlotRemove={handleRemove}
                />
            </div>
            <div className="md:hidden">
                <MealPlanMobileView
                    currentWeek={data.currentWeek}
                    nextWeek={data.nextWeek}
                    family={family}
                    onSlotClick={setEditingSlotId}
                    onSlotRemove={handleRemove}
                />
            </div>

            {editingSlot && (
                <MealSlotEditModal
                    slot={editingSlot}
                    plannedRecipeIds={plannedRecipeIds}
                    onClose={() => setEditingSlotId(null)}
                />
            )}
        </div>
    );
}
