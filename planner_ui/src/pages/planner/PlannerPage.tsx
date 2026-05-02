import {CalendarDays} from 'lucide-react';
import {useMealPlanCurrentAndNext} from '@/hooks/useMealPlan';
import {Spinner} from '@/components/ui/Spinner';
import {ErrorMessage} from '@/components/ui/ErrorMessage';
import {SectionHeader} from '@/components/ui/SectionHeader';
import {MealPlanGrid} from '@/components/planner/MealPlanGrid';

export function PlannerPage() {
    const {data, isLoading, isError, error} = useMealPlanCurrentAndNext();

    if (isLoading) return <Spinner/>;
    if (isError) return <ErrorMessage error={error} fallback="Failed to load meal plan."/>;
    if (!data) return null;

    return (
        <div className="mx-auto max-w-6xl space-y-8">
            <SectionHeader icon={CalendarDays} title="Meal Plan" level="h1"/>

            <section className="space-y-3">
                <h2 className="text-lg font-semibold">
                    Week {data.currentWeek.startDate} – {data.currentWeek.endDate}
                </h2>
                <MealPlanGrid
                    plan={data.currentWeek}
                    onSlotClick={(id) => console.log('Clicked slot', id)}
                />
            </section>

            <section className="space-y-3">
                <h2 className="text-lg font-semibold">
                    Week {data.nextWeek.startDate} – {data.nextWeek.endDate}
                </h2>
                <MealPlanGrid
                    plan={data.nextWeek}
                    onSlotClick={(id) => console.log('Clicked slot', id)}
                />
            </section>
        </div>
    );
}
