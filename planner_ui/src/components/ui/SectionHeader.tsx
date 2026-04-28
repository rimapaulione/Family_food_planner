import type {LucideIcon} from 'lucide-react';

interface SectionHeaderProps {
    icon: LucideIcon;
    title: string;
    level?: 'h1' | 'h2';
}

export function SectionHeader({icon: Icon, title, level = 'h2'}: SectionHeaderProps) {
    const className = level === 'h1'
        ? 'flex items-center gap-2 text-2xl font-bold text-foreground'
        : 'flex items-center gap-2 text-lg font-semibold text-foreground';
    const iconSize = level === 'h1' ? 'h-6 w-6' : 'h-5 w-5';
    const Tag = level;
    return (
        <Tag className={className}>
            <Icon className={iconSize}/>
            {title}
        </Tag>
    );
}
