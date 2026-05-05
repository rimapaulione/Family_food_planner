package org.example.planner_backend.dto.shopping;

import java.time.LocalDate;
import java.util.List;

public record ShoppingListResponseDto(
        LocalDate weekStart,
        LocalDate weekEnd,
        List<ShoppingItemPlanDto> items,
        List<ShoppingItemManualDto> manualItems
) {
}
