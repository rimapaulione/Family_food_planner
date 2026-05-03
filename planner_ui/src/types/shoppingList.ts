export interface ShoppingItem {
    ingredientId: string;
    name: string;
    unit: string;
    quantity: number;
    targetQuantity: number;
    isBought: boolean;
}

export interface ShoppingList {
    weekStart: string;
    weekEnd: string;
    items: ShoppingItem[];
}

export interface MarkBoughtRequest {
    weekStart: string;
    ingredientId: string;
    quantity: number;
}
