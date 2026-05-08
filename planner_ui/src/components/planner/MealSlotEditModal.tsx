import {useEffect, useMemo, useState} from 'react';
import {useForm} from 'react-hook-form';
import {zodResolver} from '@hookform/resolvers/zod';
import {AlertTriangle, Clock, Search, Star, Trash2, User, X} from 'lucide-react';
import {useCategories} from '@/hooks/useCategories';
import {useFamily} from '@/hooks/useFamily';
import {useRecipes} from '@/hooks/useRecipes';
import {useUpdateMealSlot} from '@/hooks/useMealPlan';
import {Spinner} from '@/components/ui/Spinner';
import {CATEGORY_LABELS} from '@/constants/categories';
import {LONG_COOKING_MINUTES, MEAL_LABELS} from '@/constants/mealPlan';
import {mealSlotServingsSchema, type MealSlotServingsFormData} from '@/schemas/mealSlot';
import type {MealSlot} from '@/types/mealPlan';
import {effectiveServings} from '@/utils/mealPlanHelpers';

function formatSlotDate(iso: string): string {
    return new Date(iso + 'T00:00:00Z').toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        timeZone: 'UTC',
    });
}

function isWeekday(iso: string): boolean {
    const dow = new Date(iso + 'T00:00:00Z').getUTCDay();
    return dow >= 1 && dow <= 5;
}

interface MealSlotEditModalProps {
    slot: MealSlot;
    plannedRecipeIds: Set<string>;
    onClose: () => void;
}

export function MealSlotEditModal({slot, plannedRecipeIds, onClose}: MealSlotEditModalProps) {
    const [search, setSearch] = useState('');
    const [categoryId, setCategoryId] = useState<number | null>(null);

    const {data: recipes, isLoading} = useRecipes();
    const {data: categories} = useCategories();
    const {data: family} = useFamily();
    const updateMutation = useUpdateMealSlot();

    const initialServings = family != null ? effectiveServings(slot, family) : slot.servings;

    const {register, trigger, getValues, formState: {errors}} = useForm<MealSlotServingsFormData>({
        resolver: zodResolver(mealSlotServingsSchema),
        mode: 'onChange',
        defaultValues: {
            servings: initialServings != null ? String(initialServings) : '',
        },
    });

    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose();
        };
        window.addEventListener('keydown', handler);
        return () => window.removeEventListener('keydown', handler);
    }, [onClose]);

    const filteredRecipes = useMemo(() => {
        if (!recipes) return [];
        const lowerSearch = search.toLowerCase().trim();
        return recipes
            .filter((r) => {
                if (categoryId !== null && r.category.id !== categoryId) return false;
                if (lowerSearch && !r.name.toLowerCase().includes(lowerSearch)) return false;
                return true;
            })
            .sort((a, b) => {
                const aPlanned = plannedRecipeIds.has(a.id) ? 1 : 0;
                const bPlanned = plannedRecipeIds.has(b.id) ? 1 : 0;
                return aPlanned - bPlanned;
            });
    }, [recipes, categoryId, search, plannedRecipeIds]);

    const handlePick = async (recipeId: string) => {
        const isValid = await trigger();
        if (!isValid) return;
        const {servings} = getValues();
        const servingsValue = servings === '' ? null : Number(servings);
        await updateMutation.mutateAsync({
            slotId: slot.id,
            request: {recipeId, servings: servingsValue},
        });
        onClose();
    };

    const handleSaveServings = async () => {
        const isValid = await trigger();
        if (!isValid) return;
        const {servings} = getValues();
        const servingsValue = servings === '' ? null : Number(servings);
        await updateMutation.mutateAsync({
            slotId: slot.id,
            request: {recipeId: slot.recipeId, servings: servingsValue},
        });
        onClose();
    };

    const handleClear = async () => {
        await updateMutation.mutateAsync({
            slotId: slot.id,
            request: {recipeId: null, servings: null},
        });
        onClose();
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4"
            onClick={onClose}
        >
            <div
                className="flex max-h-[90vh] w-full max-w-md flex-col rounded-lg border border-border bg-card shadow-lg"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-center justify-between border-b border-border p-4">
                    <h3 className="font-semibold">
                        Pick recipe for {MEAL_LABELS[slot.mealType]} — {formatSlotDate(slot.date)}
                    </h3>
                    <button
                        onClick={onClose}
                        className="rounded p-1 text-muted-foreground hover:bg-accent"
                    >
                        <X className="h-4 w-4"/>
                    </button>
                </div>

                <div className="space-y-3 p-4 pb-2">
                    <div className="relative">
                        <Search className="pointer-events-none absolute left-2.5 top-2 h-4 w-4 text-muted-foreground"/>
                        <input
                            type="text"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            placeholder="Search recipes..."
                            autoFocus
                            className="w-full rounded-md border border-input bg-background py-1.5 pl-8 pr-3 text-base md:text-sm outline-none focus:ring-2 focus:ring-ring"
                        />
                    </div>

                    <div className="flex items-center gap-2">
                        <label htmlFor="servings" className="text-sm text-muted-foreground">
                            Servings:
                        </label>
                        <input
                            id="servings"
                            type="number"
                            min={1}
                            max={50}
                            {...register('servings')}
                            className="w-20 rounded-md border border-input bg-background px-2 py-1 text-base outline-none focus:ring-2 focus:ring-ring md:text-sm"
                        />
                        {slot.recipeId !== null && (
                            <button
                                type="button"
                                onClick={handleSaveServings}
                                disabled={updateMutation.isPending}
                                className="rounded-md bg-primary px-3 py-1 text-sm font-medium text-primary-foreground hover:opacity-90 disabled:opacity-50"
                            >
                                Save
                            </button>
                        )}
                    </div>
                    {errors.servings && (
                        <p className="text-xs text-destructive">{errors.servings.message}</p>
                    )}

                    {categories && categories.length > 0 && (
                        <div className="flex flex-wrap gap-1">
                            <button
                                type="button"
                                onClick={() => setCategoryId(null)}
                                className={`rounded-full px-2.5 py-0.5 text-xs ${
                                    categoryId === null
                                        ? 'bg-primary text-primary-foreground'
                                        : 'bg-secondary text-secondary-foreground hover:bg-accent'
                                }`}
                            >
                                All
                            </button>
                            {categories.map((cat) => (
                                <button
                                    key={cat.id}
                                    type="button"
                                    onClick={() => setCategoryId(cat.id)}
                                    className={`rounded-full px-2.5 py-0.5 text-xs ${
                                        categoryId === cat.id
                                            ? 'bg-primary text-primary-foreground'
                                            : 'bg-secondary text-secondary-foreground hover:bg-accent'
                                    }`}
                                >
                                    {CATEGORY_LABELS[cat.name] ?? cat.name}
                                </button>
                            ))}
                        </div>
                    )}
                </div>

                <div className="no-scrollbar h-80 overflow-y-auto px-4 pb-4">
                    {isLoading ? (
                        <Spinner/>
                    ) : filteredRecipes.length === 0 ? (
                        <p className="py-4 text-center text-sm text-muted-foreground">
                            No recipes found.
                        </p>
                    ) : (
                        <div className="space-y-1">
                            {filteredRecipes.map((recipe) => {
                                const isLong = isWeekday(slot.date) && recipe.cookingTimeMinutes > LONG_COOKING_MINUTES;
                                const isPlanned = plannedRecipeIds.has(recipe.id);
                                return (
                                    <button
                                        key={recipe.id}
                                        type="button"
                                        onClick={() => handlePick(recipe.id)}
                                        disabled={updateMutation.isPending}
                                        className={`flex w-full items-center justify-between gap-2 rounded-md px-3 py-2 text-left text-sm hover:bg-accent disabled:cursor-not-allowed disabled:opacity-50 ${
                                            isPlanned ? 'opacity-50' : ''
                                        }`}
                                    >
                                        <div className="min-w-0 flex-1">
                                            <div className="flex items-center gap-1">
                                                {recipe.isFavorite && (
                                                    <Star className="h-3 w-3 shrink-0 fill-yellow-400 text-yellow-400"/>
                                                )}
                                                <span className="truncate font-medium">{recipe.name}</span>
                                                {isPlanned && (
                                                    <span className="shrink-0 text-[10px] text-muted-foreground">
                                                        (planned)
                                                    </span>
                                                )}
                                            </div>
                                            {recipe.tags.length > 0 && (
                                                <div className="mt-0.5 flex flex-wrap gap-1">
                                                    {recipe.tags.map((t) => (
                                                        <span
                                                            key={t.id}
                                                            className="rounded-full bg-secondary px-1.5 text-[10px] text-secondary-foreground"
                                                        >
                                                            {t.name}
                                                        </span>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                        <span className="flex shrink-0 items-center gap-2 text-xs text-muted-foreground">
                                            <span
                                                className={`flex items-center gap-0.5 ${
                                                    isLong ? 'font-medium text-orange-500' : ''
                                                }`}
                                            >
                                                <Clock className="h-3 w-3"/>
                                                {recipe.cookingTimeMinutes}m
                                                {isLong && <AlertTriangle className="h-3 w-3"/>}
                                            </span>
                                            <span className="flex items-center gap-0.5">
                                                <User className="h-3 w-3"/>
                                                {recipe.defaultServing}
                                            </span>
                                        </span>
                                    </button>
                                );
                            })}
                        </div>
                    )}
                </div>

                {slot.recipeId !== null && (
                    <div className="border-t border-border p-3">
                        <button
                            type="button"
                            onClick={handleClear}
                            disabled={updateMutation.isPending}
                            className="flex w-full items-center justify-center gap-1 rounded-md py-2 text-sm text-destructive hover:bg-destructive/10 disabled:opacity-50"
                        >
                            <Trash2 className="h-4 w-4"/>
                            Clear current recipe
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
