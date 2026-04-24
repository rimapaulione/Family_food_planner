package org.example.planner_backend.dto.auth;

import org.example.planner_backend.model.enums.Role;

import java.util.UUID;

public record AuthResponseDto(
          String token,                                                                       
          UUID id,
          String email,                                                                       
          String displayName,            
          Role role
  ) {                        
  }            