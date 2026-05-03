import {useState} from 'react';
import {useRecipes} from '@/hooks/useRecipes';
import {useIngredientsWithRecipeCount} from '@/hooks/useIngredients';
import {RecipesTab} from '@/components/recipes/RecipesTab';
import {IngredientsTab} from '@/components/ingredients/IngredientsTab';
import {TabBar} from '@/components/ui/TabBar';
import {TabButton} from '@/components/ui/TabButton';

export function RecipeListPage() {
    const [tab, setTab] = useState<'recipes' | 'ingredients'>('recipes');
    const {data: recipes} = useRecipes();
    const {data: ingredients} = useIngredientsWithRecipeCount();

    return (
        <div className="space-y-4">
            <TabBar>
                <TabButton label="Recipes" count={recipes?.length} isActive={tab === 'recipes'}
                           onClick={() => setTab('recipes')}/>
                <TabButton label="Ingredients" count={ingredients?.length} isActive={tab === 'ingredients'}
                           onClick={() => setTab('ingredients')}/>
            </TabBar>

            {tab === 'recipes' ? <RecipesTab/> : <IngredientsTab/>}
        </div>
    );
}
