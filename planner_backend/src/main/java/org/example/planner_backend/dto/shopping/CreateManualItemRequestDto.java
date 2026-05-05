package org.example.planner_backend.dto.shopping;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.planner_backend.model.enums.Unit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateManualItemRequestDto(
        @NotNull LocalDate weekStart,
        @NotBlank @Size(max = 100) String name,
        Unit unit,
        @DecimalMin("0.01") BigDecimal quantity
) {
}
