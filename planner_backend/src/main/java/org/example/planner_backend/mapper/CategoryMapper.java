package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.category.CategoryResponseDto;
import org.example.planner_backend.model.entity.Category;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDto toResponse(Category category);

}



