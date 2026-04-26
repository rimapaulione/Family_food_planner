package org.example.planner_backend.dto.family;

import org.example.planner_backend.model.enums.Role;

import java.util.UUID;

public record FamilyMemberDto(
        UUID userId,
        String displayName,
        String email,
        Role role
) {
}
