import type {DayOfWeek} from '@/types/family';
import {inputClass} from '@/utils/inputClass';

interface ShoppingDaySelectProps {
    value: DayOfWeek;
    onChange: (value: DayOfWeek) => void;
}

const DAYS: DayOfWeek[] = [
    'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY',
];

const formatDay = (d: DayOfWeek) => d.charAt(0) + d.slice(1).toLowerCase();

export function ShoppingDaySelect({value, onChange}: ShoppingDaySelectProps) {
    return (
        <select
            value={value}
            onChange={(e) => onChange(e.target.value as DayOfWeek)}
            className={inputClass(false)}
        >
            {DAYS.map((d) => (
                <option key={d} value={d}>{formatDay(d)}</option>
            ))}
        </select>
    );
}
