package org.example.planner_backend.dto.shopping;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MarkBoughtPlanRequestDto(
        @NotNull LocalDate weekStart,
        @NotNull UUID ingredientId,
        @NotNull @DecimalMin("0.01") BigDecimal quantity
) {
}
