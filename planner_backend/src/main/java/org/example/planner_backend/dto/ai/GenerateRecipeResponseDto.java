package org.example.planner_backend.dto.ai;

import java.util.List;

public record GenerateRecipeResponseDto(
        String name,
        Integer categoryId,
        Integer defaultServing,
        Integer cookingTimeMinutes,
        String notes,
        List<Integer> tagIds,
        List<MatchedIngredientDto> ingredients,
        List<MissingIngredientDto> missingIngredients
) {
}
