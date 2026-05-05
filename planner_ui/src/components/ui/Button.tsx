import {Link} from 'react-router-dom';
import type {LucideIcon} from 'lucide-react';
import type {ReactNode} from 'react';

const variants = {
    primary: 'bg-primary font-medium text-primary-foreground hover:opacity-90',
    success: 'bg-success font-medium text-success-foreground hover:opacity-90',
    outline: 'border border-border hover:bg-accent',
    destructive: 'border border-destructive text-destructive hover:bg-destructive hover:text-destructive-foreground',
} as const;

interface ButtonProps {
    variant?: keyof typeof variants;
    icon?: LucideIcon;
    to?: string;
    onClick?: () => void;
    type?: 'button' | 'submit';
    disabled?: boolean;
    children?: ReactNode;
    className?: string;
}

export function Button({
    variant = 'outline',
    icon: Icon,
    to,
    onClick,
    type = 'button',
    disabled,
    children,
    className = '',
}: ButtonProps) {
    const classes = `flex items-center gap-1 rounded-md px-3 py-1.5 text-sm no-underline disabled:opacity-50 ${variants[variant]} ${className}`;

    if (to) {
        return (
            <Link to={to} className={classes}>
                {Icon && <Icon className="h-3 w-3"/>}
                {children}
            </Link>
        );
    }

    return (
        <button type={type} onClick={onClick} disabled={disabled} className={classes}>
            {Icon && <Icon className="h-3 w-3"/>}
            {children}
        </button>
    );
}
