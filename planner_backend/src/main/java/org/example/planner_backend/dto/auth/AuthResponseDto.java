package org.example.planner_backend.dto.auth;

import org.example.planner_backend.model.enums.Role;

import java.util.UUID;

public record AuthResponseDto(
          String token,
          UUID id,
          String email,
          String displayName,
          String avatarUrl,
          Role role,
          UUID familyId
  ) {
  }
