package org.example.planner_backend.dto.family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FamilyRequestDto(
        @NotBlank @Size(min = 2, max = 100)
        String name
) {
}
