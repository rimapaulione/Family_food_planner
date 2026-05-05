package org.example.planner_backend.dto.shopping;

import java.math.BigDecimal;
import java.util.UUID;

public record ShoppingItemManualDto(
        UUID id,
        String name,
        String unit,
        BigDecimal quantity,
        boolean isBought
) {
}
