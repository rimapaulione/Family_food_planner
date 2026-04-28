package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.invitation.InvitationResponseDto;
import org.example.planner_backend.model.entity.FamilyInvitation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InvitationMapper {

    InvitationResponseDto toDto(FamilyInvitation invitation);
}
