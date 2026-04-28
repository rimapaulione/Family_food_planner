import type {ReactNode} from 'react';

const variants = {
    solid: 'border-border',
    dashed: 'border-dashed border-border',
} as const;

const paddings = {
    sm: 'px-3 py-2',
    md: 'p-4',
} as const;

interface CardProps {
    variant?: keyof typeof variants;
    padding?: keyof typeof paddings;
    className?: string;
    children: ReactNode;
}

export function Card({variant = 'solid', padding = 'md', className = '', children}: CardProps) {
    return (
        <div className={`rounded-md border ${variants[variant]} ${paddings[padding]} ${className}`}>
            {children}
        </div>
    );
}
