package org.example.planner_backend.service;


import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.family.FamilyRequestDto;
import org.example.planner_backend.dto.family.FamilyResponseDto;
import org.example.planner_backend.dto.family.FamilySettingsRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.mapper.FamilyMapper;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.repository.AppUserRepository;
import org.example.planner_backend.repository.FamilyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {
    private final FamilyRepository familyRepository;
    private final AppUserRepository appUserRepository;
    private final FamilyResolver familyResolver;
    private final FamilyMapper familyMapper;


    @Transactional
    public FamilyResponseDto create(final String creatorEmail, final FamilyRequestDto request) {
        AppUser creator = appUserRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        if (creator.getFamily() != null) {
            throw new ConflictException("User already has a family");
        }

        Family family = Family.builder()
                .name(request.name())
                .createdBy(creator)
                .isSetupCompleted(false)
                .build();
        family = familyRepository.save(family);

        creator.setFamily(family);
        creator.setRole(Role.ADMIN);
        appUserRepository.save(creator);

        List<AppUser> users = appUserRepository.findByFamilyId(family.getId());

        return new FamilyResponseDto(
                family.getId(),
                family.getName(),
                family.getShoppingDay(),
                family.getDefaultWeekdayServings(),
                family.getDefaultWeekendServings(),
                family.isSetupCompleted(),
                familyMapper.toMembers(users)
        );
    }

    @Transactional(readOnly = true)
    public FamilyResponseDto getMyFamily(final String email) {
        Family family = familyResolver.getFamilyByEmail(email);
        List<AppUser> members = appUserRepository.findByFamilyId(family.getId());

        return new FamilyResponseDto(
                family.getId(),
                family.getName(),
                family.getShoppingDay(),
                family.getDefaultWeekdayServings(),
                family.getDefaultWeekendServings(),
                family.isSetupCompleted(),
                familyMapper.toMembers(members)
        );
    }

    @Transactional
    public FamilyResponseDto updateSettings(final String email,
                                            final FamilySettingsRequestDto request) {
        Family family = familyResolver.getFamilyByEmail(email);
        family.setName(request.name());
        family.setShoppingDay(request.shoppingDay());
        family.setDefaultWeekdayServings(request.defaultWeekdayServings());
        family.setDefaultWeekendServings(request.defaultWeekendServings());
        family.setSetupCompleted(request.isSetupCompleted());

        List<AppUser> members =
                appUserRepository.findByFamilyId(family.getId());

        return new FamilyResponseDto(
                family.getId(),
                family.getName(),
                family.getShoppingDay(),
                family.getDefaultWeekdayServings(),
                family.getDefaultWeekendServings(),
                family.isSetupCompleted(),
                familyMapper.toMembers(members)
        );
    }


}
