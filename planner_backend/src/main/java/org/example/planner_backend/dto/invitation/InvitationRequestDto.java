package org.example.planner_backend.dto.invitation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record InvitationRequestDto(
        @NotBlank @Email
        String email
) {
}