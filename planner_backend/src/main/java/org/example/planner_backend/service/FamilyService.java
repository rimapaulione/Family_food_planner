package org.example.planner_backend.service;


import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.family.FamilyMemberRoleRequestDto;
import org.example.planner_backend.dto.family.FamilyRequestDto;
import org.example.planner_backend.dto.family.FamilyResponseDto;
import org.example.planner_backend.dto.family.FamilySettingsRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.mapper.FamilyMapper;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.model.entity.IngredientTemplate;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.repository.AppUserRepository;
import org.example.planner_backend.repository.FamilyRepository;
import org.example.planner_backend.repository.IngredientRepository;
import org.example.planner_backend.repository.IngredientTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FamilyService {
    private final FamilyRepository familyRepository;
    private final AppUserRepository appUserRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientTemplateRepository ingredientTemplateRepository;
    private final FamilyResolver familyResolver;
    private final FamilyMapper familyMapper;


    @Transactional
    public FamilyResponseDto create(final String creatorEmail, final FamilyRequestDto request) {
        AppUser creator = familyResolver.getUserByEmail(creatorEmail);

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

        this.copySeedIngredients(family);

        return this.buildFamilyResponse(family);
    }

    @Transactional(readOnly = true)
    public FamilyResponseDto getMyFamily(final String email) {
        Family family = familyResolver.getFamilyByEmail(email);
        return this.buildFamilyResponse(family);
    }

    @Transactional
    public FamilyResponseDto updateSettings(final String email,
                                            final FamilySettingsRequestDto request) {
        Family family = familyResolver.getAdminUser(email).getFamily();
        family.setName(request.name());
        family.setShoppingDay(request.shoppingDay());
        family.setDefaultWeekdayServings(request.defaultWeekdayServings());
        family.setDefaultWeekendServings(request.defaultWeekendServings());
        family.setNoRepeatRecipeDays(request.noRepeatRecipeDays());
        family.setMaxWeekdayCookingMinutes(request.maxWeekdayCookingMinutes());
        family.setMaxWeekendCookingMinutes(request.maxWeekendCookingMinutes());
        family.setSetupCompleted(request.isSetupCompleted());

        return this.buildFamilyResponse(family);
    }

    @Transactional
    public FamilyResponseDto removeMember(final String email, final UUID id) {
        AppUser admin = familyResolver.getAdminUser(email);
        AppUser member = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member does not exist"));

        if (member.getFamily() == null
                || !member.getFamily().getId().equals(admin.getFamily().getId())) {
            throw new UnauthorizedException("Member is not in your family");
        }
        if (admin.getId().equals(member.getId())) {
            throw new ConflictException("You cannot remove yourself");
        }
        if (member.getRole() == Role.ADMIN) {
            long adminCount = appUserRepository.countByFamilyIdAndRole(admin.getFamily().getId(), Role.ADMIN);
            if (adminCount <= 1) {
                throw new ConflictException("Family must have at least one admin");
            }
        }

        Family family = admin.getFamily();
        member.setFamily(null);
        member.setRole(Role.USER);

        return this.buildFamilyResponse(family);
    }

    @Transactional
    public FamilyResponseDto updateMemberRole(final String email,
                                              final UUID id,
                                              final FamilyMemberRoleRequestDto request) {
        AppUser admin = familyResolver.getAdminUser(email);
        AppUser member = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member does not exist"));

        if (member.getFamily() == null
                || !member.getFamily().getId().equals(admin.getFamily().getId())) {
            throw new UnauthorizedException("Member is not in your family");
        }
        if (admin.getId().equals(member.getId())) {
            throw new ConflictException("You cannot change your own role");
        }

        long adminCount = appUserRepository.countByFamilyIdAndRole(admin.getFamily().getId(), Role.ADMIN);
        if (member.getRole() == Role.ADMIN
                && request.role() == Role.USER
                && adminCount <= 1) {
            throw new ConflictException("Family must have at least one admin");
        }

        member.setRole(request.role());

        return this.buildFamilyResponse(admin.getFamily());

    }

    private FamilyResponseDto buildFamilyResponse(Family family) {
        List<AppUser> members = appUserRepository.findByFamilyId(family.getId());
        return new FamilyResponseDto(
                family.getId(),
                family.getName(),
                family.getShoppingDay(),
                family.getDefaultWeekdayServings(),
                family.getDefaultWeekendServings(),
                family.getNoRepeatRecipeDays(),
                family.getMaxWeekdayCookingMinutes(),
                family.getMaxWeekendCookingMinutes(),
                family.isSetupCompleted(),
                familyMapper.toMembers(members)
        );
    }

    private void copySeedIngredients(final Family family) {
        List<IngredientTemplate> templates = ingredientTemplateRepository.findAll();
        List<Ingredient> seeds = templates.stream()
                .map(t -> Ingredient.builder()
                        .nameLt(t.getNameLt())
                        .unit(t.getUnit())
                        .family(family)
                        .build())
                .toList();
        ingredientRepository.saveAll(seeds);
    }
}
