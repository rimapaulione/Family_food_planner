package org.example.planner_backend.dto.family;

import org.example.planner_backend.model.enums.Role;

public record FamilyMemberRoleRequestDto(
        Role role
) {
}
