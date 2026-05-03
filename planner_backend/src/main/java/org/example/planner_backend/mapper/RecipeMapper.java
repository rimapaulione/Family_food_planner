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

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.nameLt")
    @Mapping(target = "unit", source = "ingredient.unit")
    RecipeResponseDto.RecipeIngredientDto toIngredientDto(RecipeIngredient ri);
}