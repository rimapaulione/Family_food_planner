package org.example.planner_backend.dto.recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record RecipeRequestDto(
        @NotBlank @Size(max = 100)
        String name,

        @NotNull
        Long categoryId,

        @NotNull @Min(1) @Max(50)
        Short defaultServing,

        @NotNull @Min(1) @Max(500)
        Short cookingTimeMinutes,

        @Size(max = 20)
        Set<Long> tagIds,

        UUID leftoverRecipeId,

        Boolean isFavorite,

        @Size(max = 1000)
        String notes,

        @Valid @Size(max = 50)
        List<RecipeIngredientRequestDto> ingredients
) {

    public record RecipeIngredientRequestDto(
            @NotNull
            UUID ingredientId,

            @NotNull @DecimalMin("0.01")
            BigDecimal quantity
    ) {
    }
}
