import {Star} from 'lucide-react';

interface FavoriteStarProps {
    isFavorite: boolean;
    className?: string;
}

export function FavoriteStar({isFavorite, className = 'h-3.5 w-3.5'}: FavoriteStarProps) {
    if (!isFavorite) return null;
    return <Star className={`fill-yellow-400 text-yellow-400 ${className}`}/>;
}
