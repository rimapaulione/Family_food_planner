import type {ReactNode} from 'react';

interface TabBarProps {
    children: ReactNode;
    className?: string;
}

export function TabBar({children, className = ''}: TabBarProps) {
    return (
        <div className={`flex items-center gap-4 border-b border-border ${className}`}>
            {children}
        </div>
    );
}
