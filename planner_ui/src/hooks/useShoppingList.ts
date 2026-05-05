import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import api from '@/api/axios';
import {useAuthStore} from '@/stores/useAuthStore';
import type {
    CreateManualItemRequest,
    ManualShoppingItem,
    MarkBoughtPlanRequest,
    ShoppingItem,
    ShoppingList,
} from '@/types/shoppingList';
import {getErrorMessage} from '@/utils/getErrorMessage';

const queryKey = (weekStart: string) => ['shopping-list', weekStart] as const;

export function useShoppingList(weekStart: string) {
    const familyId = useAuthStore((s) => s.familyId);
    return useQuery({
        queryKey: queryKey(weekStart),
        queryFn: async () => {
            const {data} = await api.get<ShoppingList>('/shopping-lists/family', {
                params: {weekStart},
            });
            return data;
        },
        enabled: familyId !== null,
        staleTime: 0,
        refetchInterval: 5_000,
        refetchIntervalInBackground: false,
        refetchOnWindowFocus: true,
    });
}

export function useToggleBought(weekStart: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (item: ShoppingItem) => {
            const body: MarkBoughtPlanRequest = {
                weekStart,
                ingredientId: item.ingredientId,
                quantity: item.targetQuantity,
            };
            if (item.isBought) {
                await api.delete('/shopping-lists/checks', {data: body});
            } else {
                await api.post('/shopping-lists/checks', body);
            }
        },
        onMutate: async (item) => {
            await queryClient.cancelQueries({queryKey: queryKey(weekStart)});
            const previous = queryClient.getQueryData<ShoppingList>(queryKey(weekStart));
            queryClient.setQueryData<ShoppingList>(queryKey(weekStart), (old) => {
                if (!old) return old;
                return {
                    ...old,
                    items: old.items.map((i) =>
                        i.ingredientId === item.ingredientId && i.isBought === item.isBought
                            ? {...i, isBought: !i.isBought}
                            : i,
                    ),
                };
            });
            return {previous};
        },
        onError: (err, _item, context) => {
            if (context?.previous) {
                queryClient.setQueryData(queryKey(weekStart), context.previous);
            }
            toast.error(getErrorMessage(err, 'Failed to update item'));
        },
        onSettled: () => {
            queryClient.invalidateQueries({queryKey: queryKey(weekStart)});
        },
    });
}

export function useAddManualItem(weekStart: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (name: string) => {
            const body: CreateManualItemRequest = {weekStart, name};
            const {data} = await api.post<ManualShoppingItem>('/shopping-lists/manual', body);
            return data;
        },
        onSuccess: (created) => {
            queryClient.setQueryData<ShoppingList>(queryKey(weekStart), (old) => {
                if (!old) return old;
                return {...old, manualItems: [...old.manualItems, created]};
            });
        },
        onError: (err) => {
            toast.error(getErrorMessage(err, 'Failed to add item'));
        },
    });
}

export function useToggleManualBought(weekStart: string) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (item: ManualShoppingItem) => {
            await api.patch(`/shopping-lists/manual/${item.id}`, {isBought: !item.isBought});
        },
        onMutate: async (item) => {
            await queryClient.cancelQueries({queryKey: queryKey(weekStart)});
            const previous = queryClient.getQueryData<ShoppingList>(queryKey(weekStart));
            queryClient.setQueryData<ShoppingList>(queryKey(weekStart), (old) => {
                if (!old) return old;
                return {
                    ...old,
                    manualItems: old.manualItems.map((i) =>
                        i.id === item.id ? {...i, isBought: !i.isBought} : i,
                    ),
                };
            });
            return {previous};
        },
        onError: (err, _item, context) => {
            if (context?.previous) {
                queryClient.setQueryData(queryKey(weekStart), context.previous);
            }
            toast.error(getErrorMessage(err, 'Failed to update item'));
        },
        onSettled: () => {
            queryClient.invalidateQueries({queryKey: queryKey(weekStart)});
        },
    });
}
