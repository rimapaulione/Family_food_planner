import {useState} from 'react';
import {Lock, Unlock} from 'lucide-react';
import {Button} from '@/components/ui/Button';
import {TabBar} from '@/components/ui/TabBar';
import {TabButton} from '@/components/ui/TabButton';
import {DayTabs} from './DayTabs';
import {MealRow} from './MealRow';
import {MEAL_ORDER} from '@/constants/mealPlan';
import {useUpdateMealPlan} from '@/hooks/useMealPlan';
import {useAuthStore} from '@/stores/useAuthStore';
import {ROLE} from '@/types/auth';
import type {Family} from '@/types/family';
import type {MealPlan} from '@/types/mealPlan';
import {PLAN_STATUS} from '@/types/mealPlan';
import {getWeekDates} from '@/utils/mealPlanHelpers';

const ACTIVE_DAY_STORAGE_KEY = 'planner-active-day';

interface MealPlanMobileViewProps {
    currentWeek: MealPlan;
    nextWeek: MealPlan;
    family: Family;
    onSlotClick: (slotId: string) => void;
    onSlotRemove: (slotId: string) => void;
}

export function MealPlanMobileView({
    currentWeek,
    nextWeek,
    family,
    onSlotClick,
    onSlotRemove,
}: MealPlanMobileViewProps) {{
    const isAdmin = useAuthStore((s) => s.role === ROLE.ADMIN);
    const updatePlanMutation = useUpdateMealPlan();

    const currentDays = getWeekDates(currentWeek.startDate);
    const nextDays = getWeekDates(nextWeek.startDate);

    const [activeDayIso, setActiveDayIsoState] = useState<string>(() => {
        const allDays = [...currentDays, ...nextDays];
        const stored = sessionStorage.getItem(ACTIVE_DAY_STORAGE_KEY);
        if (stored && allDays.includes(stored)) return stored;
        const today = new Date().toISOString().slice(0, 10);
        return allDays.includes(today) ? today : allDays[0];
    });

    const setActiveDayIso = (iso: string) => {
        sessionStorage.setItem(ACTIVE_DAY_STORAGE_KEY, iso);
        setActiveDayIsoState(iso);
    };

    const isCurrentWeekActive = currentDays.includes(activeDayIso);
    const activePlan = isCurrentWeekActive ? currentWeek : nextWeek;
    const days = isCurrentWeekActive ? currentDays : nextDays;
    const activeDayIndex = days.findIndex((d) => d === activeDayIso);
    const locked = activePlan.status === PLAN_STATUS.LOCKED;

    const toggleLock = () => {
        const nextStatus = locked ? PLAN_STATUS.DRAFT : PLAN_STATUS.LOCKED;
        updatePlanMutation.mutate({planId: activePlan.id, request: {status: nextStatus}});
    };

    return (
        <div className="space-y-3">
            <TabBar>
                <TabButton
                    label="This week"
                    isActive={isCurrentWeekActive}
                    onClick={() => setActiveDayIso(currentDays[0])}
                />
                <TabButton
                    label="Next week"
                    isActive={!isCurrentWeekActive}
                    onClick={() => setActiveDayIso(nextDays[0])}
                />
            </TabBar>

            <div className="flex items-center justify-between gap-2">
                <div className="text-sm text-muted-foreground">
                    {activePlan.startDate} – {activePlan.endDate}
                    {locked && <span className="ml-2">(locked)</span>}
                </div>
                {isAdmin && (
                    <Button
                        variant={locked ? 'outline' : 'primary'}
                        icon={locked ? Unlock : Lock}
                        onClick={toggleLock}
                        disabled={updatePlanMutation.isPending}
                    >
                        {locked ? 'Unlock' : 'Lock'}
                    </Button>
                )}
            </div>

            <DayTabs
                dates={days}
                activeIndex={activeDayIndex}
                onSelect={(idx) => setActiveDayIso(days[idx])}
            />

            <div className="space-y-1.5">
                {MEAL_ORDER.map((mealType) => {
                    const slot = activePlan.slots.find(
                        (s) => s.date === activeDayIso && s.mealType === mealType,
                    );
                    if (!slot) return null;
                    return (
                        <MealRow
                            key={`${mealType}-${activeDayIso}`}
                            slot={slot}
                            family={family}
                            onSlotClick={locked ? undefined : onSlotClick}
                            onSlotRemove={locked ? undefined : onSlotRemove}
                            disabled={locked}
                        />
                    );
                })}
            </div>
        </div>
    );
}
