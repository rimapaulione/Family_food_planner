package org.example.planner_backend.dto.ingredient;

import java.util.List;

public record IngredientCheckNameResponseDto(
        boolean exactMatch,
        String existingName,
        List<String> similarNames
) {
}