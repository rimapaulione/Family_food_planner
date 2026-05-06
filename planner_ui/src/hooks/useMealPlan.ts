import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import {useAuthStore} from '@/stores/useAuthStore';
import type {
    MealPlan,
    MealPlanUpdateRequest,
    MealPlanWindow,
    MealSlot,
    MealSlotUpdateRequest,
} from '@/types/mealPlan';
import {PLAN_STATUS} from '@/types/mealPlan';
import {getErrorMessage} from '@/utils/getErrorMessage';

const WINDOW_KEY = ['meal-plan', 'current-and-next'] as const;

export function useMealPlanCurrentAndNext() {
    const familyId = useAuthStore((s) => s.familyId);
    return useQuery({
        queryKey: WINDOW_KEY,
        queryFn: async () => {
            const {data} = await api.get<MealPlanWindow>('/meal-plans/current-and-next');
            return data;
        },
        enabled: familyId !== null,
        staleTime: 0,
    });
}

export function useUpdateMealSlot() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({slotId, request}: {slotId: string; request: MealSlotUpdateRequest}) => {
            const {data} = await api.patch<MealSlot>(`/meal-plans/slots/${slotId}`, request);
            return data;
        },
        onSuccess: (updatedSlot) => {
            queryClient.setQueryData<MealPlanWindow>(WINDOW_KEY, (old) => {
                if (!old) return old;
                return {
                    currentWeek: replaceSlot(old.currentWeek, updatedSlot),
                    nextWeek: replaceSlot(old.nextWeek, updatedSlot),
                };
            });
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to update slot')),
    });
}

export function useUpdateMealPlan() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async ({planId, request}: {planId: string; request: MealPlanUpdateRequest}) => {
            const {data} = await api.patch<MealPlan>(`/meal-plans/${planId}`, request);
            return data;
        },
        onSuccess: (updatedPlan) => {
            queryClient.setQueryData<MealPlanWindow>(WINDOW_KEY, (old) => {
                if (!old) return old;
                return {
                    currentWeek: old.currentWeek.id === updatedPlan.id ? updatedPlan : old.currentWeek,
                    nextWeek: old.nextWeek.id === updatedPlan.id ? updatedPlan : old.nextWeek,
                };
            });
            toast.success(updatedPlan.status === PLAN_STATUS.LOCKED ? 'Plan locked' : 'Plan unlocked');
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Failed to update plan')),
    });
}

export function useAutoFillPlan() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (planId: string) => {
            const {data} = await api.post<MealPlan>(`/meal-plans/${planId}/auto-fill`);
            return data;
        },
        onSuccess: (updatedPlan) => {
            queryClient.setQueryData<MealPlanWindow>(WINDOW_KEY, (old) => {
                if (!old) return old;
                return {
                    currentWeek: old.currentWeek.id === updatedPlan.id ? updatedPlan : old.currentWeek,
                    nextWeek: old.nextWeek.id === updatedPlan.id ? updatedPlan : old.nextWeek,
                };
            });
        },
        onError: (err) => toast.error(getErrorMessage(err, 'Auto-fill failed')),
    });
}

function replaceSlot(plan: MealPlan, updated: MealSlot): MealPlan {
    return {
        ...plan,
        slots: plan.slots.map((s) => (s.id === updated.id ? updated : s)),
    };
}
