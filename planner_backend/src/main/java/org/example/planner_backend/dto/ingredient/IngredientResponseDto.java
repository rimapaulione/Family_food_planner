package org.example.planner_backend.dto.ingredient;

import org.example.planner_backend.model.enums.Unit;

import java.util.UUID;

public record IngredientResponseDto(
        UUID id,
        String nameLt,
        Unit unit
) {
}
