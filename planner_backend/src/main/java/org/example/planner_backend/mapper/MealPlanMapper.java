package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.mealplan.MealPlanResponseDto;
import org.example.planner_backend.dto.mealplan.MealSlotResponseDto;
import org.example.planner_backend.model.entity.MealPlan;
import org.example.planner_backend.model.entity.MealSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MealPlanMapper {

    @Mapping(target = "recipeId", source = "recipe.id")
    @Mapping(target = "recipeName", source = "recipe.name")
    MealSlotResponseDto toSlotDto(MealSlot slot);

    List<MealSlotResponseDto> toSlotDtos(List<MealSlot> slots);

    MealPlanResponseDto toPlanDto(MealPlan plan);
}
