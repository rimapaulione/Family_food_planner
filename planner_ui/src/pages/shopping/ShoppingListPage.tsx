import {useState} from 'react';
import {ShoppingCart} from 'lucide-react';
import {SectionHeader} from '@/components/ui/SectionHeader';
import {TabBar} from '@/components/ui/TabBar';
import {TabButton} from '@/components/ui/TabButton';
import {ShoppingListView} from '@/components/shopping/ShoppingListView';
import {addWeeks, mondayOfThisWeek} from '@/utils/mealPlanHelpers';

export function ShoppingListPage() {
    const currentMonday = mondayOfThisWeek();
    const nextMonday = addWeeks(currentMonday, 1);
    const [tab, setTab] = useState<'current' | 'next'>('current');
    const weekStart = tab === 'current' ? currentMonday : nextMonday;

    return (
        <div className="mx-auto max-w-3xl space-y-4">
            <SectionHeader icon={ShoppingCart} title="Shopping List" level="h1"/>

            <TabBar>
                <TabButton
                    label="This week"
                    isActive={tab === 'current'}
                    onClick={() => setTab('current')}
                />
                <TabButton
                    label="Next week"
                    isActive={tab === 'next'}
                    onClick={() => setTab('next')}
                />
            </TabBar>

            <ShoppingListView weekStart={weekStart}/>
        </div>
    );
}
