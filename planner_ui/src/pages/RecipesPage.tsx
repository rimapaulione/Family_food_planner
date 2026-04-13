import {useState} from 'react';
import {useRecipes} from '@/hooks/useRecipes';
import {useIngredients} from '@/hooks/useIngredients';
import {RecipesTab} from '@/components/recipes/RecipesTab';
import {IngredientsTab} from '@/components/ingredients/IngredientsTab';
import {TabButton} from '@/components/recipes/TabButton';

export function RecipesPage() {
    const [tab, setTab] = useState<'recipes' | 'ingredients'>('recipes');
    const {data: recipes} = useRecipes();
    const {data: ingredients} = useIngredients();

    return (
        <div className="space-y-4">
            <div className="flex items-center gap-4 border-b border-border">
                <TabButton label="Recipes" count={recipes?.length} isActive={tab === 'recipes'}
                           onClick={() => setTab('recipes')}/>
                <TabButton label="Ingredients" count={ingredients?.length} isActive={tab === 'ingredients'}
                           onClick={() => setTab('ingredients')}/>
            </div>

            {tab === 'recipes' ? <RecipesTab/> : <IngredientsTab/>}
        </div>
    );
}
