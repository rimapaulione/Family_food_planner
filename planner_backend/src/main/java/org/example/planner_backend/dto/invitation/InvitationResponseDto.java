package org.example.planner_backend.dto.invitation;

import org.example.planner_backend.model.enums.InvitationStatus;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponseDto(

        UUID id,
        String invitedEmail,
        InvitationStatus status,
        String token,
        Instant createdAt,
        Instant expiresAt
) {
}
