import {UtensilsCrossed, CalendarDays, ShoppingCart, Users} from 'lucide-react';
import type {LucideIcon} from 'lucide-react';

export interface NavItem {
    to: string;
    icon: LucideIcon;
    label: string;
}

export const navItems: NavItem[] = [
    {to: '/recipes', icon: UtensilsCrossed, label: 'Recipes'},
    {to: '/planner', icon: CalendarDays, label: 'Planner'},
    {to: '/shopping', icon: ShoppingCart, label: 'Shopping'},
    {to: '/family', icon: Users, label: 'Family'},
];
