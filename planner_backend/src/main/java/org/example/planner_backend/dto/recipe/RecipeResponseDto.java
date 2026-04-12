package org.example.planner_backend.dto.recipe;

import org.example.planner_backend.dto.category.CategoryResponseDto;
import org.example.planner_backend.dto.tag.TagResponseDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record RecipeResponseDto(
        UUID id,
        String name,
        CategoryResponseDto category,
        Short defaultServing,
        Short cookingTimeMinutes,
        Set<TagResponseDto> tags,
        List<RecipeIngredientDto> ingredients,
        UUID leftoverRecipeId,
        Boolean isFavorite,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {

    public record RecipeIngredientDto(
            UUID id,
            UUID ingredientId,
            String ingredientName,
            String unit,
            BigDecimal quantity
    ) {
    }
}
