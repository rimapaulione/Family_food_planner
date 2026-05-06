package org.example.planner_backend.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateRecipeRequestDto(
        @NotBlank @Size(max = 500) String prompt
) {
}
