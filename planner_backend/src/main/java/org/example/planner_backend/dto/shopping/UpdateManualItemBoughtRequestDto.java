package org.example.planner_backend.dto.shopping;

import jakarta.validation.constraints.NotNull;

public record UpdateManualItemBoughtRequestDto(
        @NotNull Boolean isBought
) {
}
