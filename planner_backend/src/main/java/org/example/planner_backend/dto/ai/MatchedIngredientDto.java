package org.example.planner_backend.dto.ai;

import java.math.BigDecimal;
import java.util.UUID;

public record MatchedIngredientDto(
        UUID ingredientId,
        BigDecimal quantity,
        String unit
) {
}
