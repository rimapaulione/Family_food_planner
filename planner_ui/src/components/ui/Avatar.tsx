import {UserCircle} from 'lucide-react';

interface AvatarProps {
    src: string | null;
    size?: 'sm' | 'md' | 'lg';
}

const sizeClass = {
    sm: 'h-7 w-7',
    md: 'h-10 w-10',
    lg: 'h-16 w-16',
} as const;

export function Avatar({src, size = 'md'}: AvatarProps) {
    const cls = sizeClass[size];
    if (src) {
        return <img src={src} alt="" className={`${cls} rounded-full object-cover`}/>;
    }
    return <UserCircle className={`${cls} text-muted-foreground`}/>;
}
