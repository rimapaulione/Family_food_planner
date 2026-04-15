package org.example.planner_backend.dto.ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.planner_backend.model.enums.Unit;

public record IngredientRequestDto(
        @NotBlank @Size(max = 100)
        String nameLt,
        @NotNull
        Unit unit
) {
}
