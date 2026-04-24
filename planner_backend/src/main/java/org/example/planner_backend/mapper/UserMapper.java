package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.user.UserResponseDto;
import org.example.planner_backend.model.entity.AppUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toResponse(AppUser user);
}