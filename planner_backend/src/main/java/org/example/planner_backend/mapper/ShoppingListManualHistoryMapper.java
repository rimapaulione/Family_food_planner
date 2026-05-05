package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.shopping.ShoppingItemManualDto;
import org.example.planner_backend.model.entity.ShoppingListManualHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShoppingListManualHistoryMapper {

    @Mapping(source = "bought", target = "isBought")
    ShoppingItemManualDto toDto(ShoppingListManualHistory entity);
}
