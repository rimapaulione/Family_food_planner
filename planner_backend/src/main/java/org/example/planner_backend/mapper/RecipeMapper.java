package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.recipe.RecipeResponseDto;
import org.example.planner_backend.model.entity.Recipe;
import org.example.planner_backend.model.entity.RecipeIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class,
        TagMapper.class})
public interface RecipeMapper {

    @Mapping(source = "leftoverRecipe.id", target = "leftoverRecipeId")
    RecipeResponseDto toResponse(Recipe recipe);

    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.nameLt", target = "ingredientName")
    @Mapping(source = "ingredient.unit", target = "unit")
    RecipeResponseDto.RecipeIngredientDto toRecipeIngredientDto(RecipeIngredient
                                                                        recipeIngredient);
}