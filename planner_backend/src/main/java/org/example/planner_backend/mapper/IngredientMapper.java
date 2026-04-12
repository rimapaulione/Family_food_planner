package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.ingredient.IngredientResponseDto;
import org.example.planner_backend.model.entity.Ingredient;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
    IngredientResponseDto toResponse(Ingredient ingredient);


}
