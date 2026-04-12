package org.example.planner_backend.dto.recipe;

import org.example.planner_backend.dto.category.CategoryResponseDto;
import org.example.planner_backend.dto.tag.TagResponseDto;

import java.util.Set;
import java.util.UUID;

public record RecipeListResponseDto(
        UUID id,
        String name,
        CategoryResponseDto category,
        Short defaultServing,
        Short cookingTimeMinutes,
        Set<TagResponseDto> tags,
        Boolean isFavorite,
        int ingredientCount
) {
}
