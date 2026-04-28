import {UserCircle} from 'lucide-react';

interface AvatarProps {
    src: string | null;
    size?: 'sm' | 'md' | 'lg';
    initials?: string;
}

const sizeClass = {
    sm: 'h-7 w-7',
    md: 'h-10 w-10',
    lg: 'h-12 w-12 sm:h-16 sm:w-16',
} as const;

const textClass = {
    sm: 'text-xs',
    md: 'text-sm',
    lg: 'text-lg',
} as const;

export function Avatar({src, size = 'md', initials}: AvatarProps) {
    const cls = sizeClass[size];
    if (src) {
        return <img src={src} alt="" className={`${cls} rounded-full object-cover`}/>;
    }
    if (initials) {
        return (
            <div
                className={`${cls} flex flex-none items-center justify-center rounded-full bg-secondary font-bold text-secondary-foreground ${textClass[size]}`}
            >
                {initials}
            </div>
        );
    }
    return <UserCircle className={`${cls} text-muted-foreground`}/>;
}
