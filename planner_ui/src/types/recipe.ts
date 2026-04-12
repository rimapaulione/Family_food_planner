export interface Category {
  id: number;
  name: string;
}

export interface Tag {
  id: number;
  name: string;
}

export interface RecipeIngredient {
  id: string;
  ingredientId: string;
  ingredientName: string;
  unit: string;
  quantity: number;
}

export interface RecipeListResponse {
  id: string;
  name: string;
  category: Category;
  defaultServing: number;
  cookingTimeMinutes: number;
  tags: Tag[];
  isFavorite: boolean;
  ingredientCount: number;
}

export interface RecipeResponse {
  id: string;
  name: string;
  category: Category;
  defaultServing: number;
  cookingTimeMinutes: number;
  tags: Tag[];
  ingredients: RecipeIngredient[];
  leftoverRecipeId: string | null;
  isFavorite: boolean;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface RecipeIngredientRequest {
  ingredientId: string;
  quantity: number;
}

export interface RecipeRequest {
  name: string;
  categoryId: number;
  defaultServing?: number;
  cookingTimeMinutes: number;
  tagIds?: number[];
  leftoverRecipeId?: string | null;
  isFavorite?: boolean;
  notes?: string;
  ingredients?: RecipeIngredientRequest[];
}
