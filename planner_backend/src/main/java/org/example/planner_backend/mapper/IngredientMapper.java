package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientResponseDto;
import org.example.planner_backend.model.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
    IngredientResponseDto toResponse(Ingredient ingredient);

    @Mapping(source = "ingredient.id", target = "id")
    @Mapping(source = "ingredient.nameLt", target = "nameLt")
    @Mapping(source = "ingredient.unit", target = "unit")
    @Mapping(source = "recipeCount", target = "recipeCount")
    IngredientDetailResponseDto toDetailResponse(Ingredient ingredient, int recipeCount);
}
