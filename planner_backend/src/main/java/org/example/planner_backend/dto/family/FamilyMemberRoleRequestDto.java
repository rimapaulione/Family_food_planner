package org.example.planner_backend.dto.family;

import jakarta.validation.constraints.NotNull;
import org.example.planner_backend.model.enums.Role;

public record FamilyMemberRoleRequestDto(
        @NotNull
        Role role
) {
}
