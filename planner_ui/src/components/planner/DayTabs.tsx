import {TabBar} from '@/components/ui/TabBar';
import {TabButton} from '@/components/ui/TabButton';
import {formatDayOfMonth, formatWeekdayShort} from '@/utils/mealPlanHelpers';

interface DayTabsProps {
    dates: string[];
    activeIndex: number;
    onSelect: (index: number) => void;
}

export function DayTabs({dates, activeIndex, onSelect}: DayTabsProps) {
    return (
        <TabBar className="overflow-x-auto">
            {dates.map((date, idx) => (
                <TabButton
                    key={date}
                    label={`${formatWeekdayShort(date)} ${formatDayOfMonth(date)}`}
                    isActive={activeIndex === idx}
                    onClick={() => onSelect(idx)}
                />
            ))}
        </TabBar>
    );
}
