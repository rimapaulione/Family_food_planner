import {TabButton} from '@/components/ui/TabButton';
import {formatDayOfMonth, formatWeekdayShort} from '@/utils/mealPlanHelpers';

interface DayTabsProps {
    dates: string[];
    activeIndex: number;
    onSelect: (index: number) => void;
}

export function DayTabs({dates, activeIndex, onSelect}: DayTabsProps) {
    return (
        <div className="flex gap-3 overflow-x-auto border-b border-border">
            {dates.map((date, idx) => (
                <TabButton
                    key={date}
                    label={`${formatWeekdayShort(date)} ${formatDayOfMonth(date)}`}
                    isActive={activeIndex === idx}
                    onClick={() => onSelect(idx)}
                />
            ))}
        </div>
    );
}
