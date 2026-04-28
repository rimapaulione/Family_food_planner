package org.example.planner_backend.dto.invitation;

import org.example.planner_backend.model.enums.InvitationStatus;

public record InvitationPublicDto(
        String familyName,
        String invitedEmail,
        InvitationStatus status
) {
}
