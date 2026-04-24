package org.example.planner_backend.dto.user;

import org.example.planner_backend.model.enums.Role;

import java.util.UUID;

public record UserResponseDto(

        UUID id,
        String email,
        String displayName,
        String avatarUrl,
        Role role
) {
}
