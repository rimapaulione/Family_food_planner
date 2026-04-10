package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.tag.TagResponseDto;
import org.example.planner_backend.model.entity.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagResponseDto toResponse(Tag tag);
}
