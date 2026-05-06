package org.example.planner_backend.dto.ai;

import java.math.BigDecimal;

public record MissingIngredientDto(
        String name,
        BigDecimal quantity,
        String unit
) {
}
