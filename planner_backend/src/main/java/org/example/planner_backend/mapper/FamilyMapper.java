package org.example.planner_backend.mapper;

import org.example.planner_backend.dto.family.FamilyMemberDto;
import org.example.planner_backend.model.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FamilyMapper {

    List<FamilyMemberDto> toMembers(List<AppUser> users);
}
