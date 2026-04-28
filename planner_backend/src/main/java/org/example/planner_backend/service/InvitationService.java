package org.example.planner_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.family.FamilyResponseDto;
import org.example.planner_backend.dto.invitation.InvitationRequestDto;
import org.example.planner_backend.dto.invitation.InvitationResponseDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.mapper.InvitationMapper;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.FamilyInvitation;
import org.example.planner_backend.model.enums.InvitationStatus;
import org.example.planner_backend.repository.AppUserRepository;
import org.example.planner_backend.repository.InvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private static final int EXPIRY_DAYS = 7;

    private final InvitationRepository invitationRepository;
    private final AppUserRepository appUserRepository;
    private final FamilyResolver familyResolver;
    private final FamilyService familyService;
    private final InvitationMapper invitationMapper;


    @Transactional
    public InvitationResponseDto invite(final String email, final InvitationRequestDto request) {
        AppUser admin = familyResolver.getAdminUser(email);
        Family family = admin.getFamily();

        String invitedEmail = request.email().trim().toLowerCase();

        if (invitedEmail.equalsIgnoreCase(email)) {
            throw new ConflictException("You cannot invite yourself");
        }

        appUserRepository.findByEmail(invitedEmail).ifPresent(u -> {
            if (u.getFamily() != null) {
                throw new ConflictException("User already belongs to a family");
            }
        });

        if (invitationRepository.existsByFamilyIdAndInvitedEmailAndStatus(
                family.getId(), invitedEmail, InvitationStatus.PENDING)) {
            throw new ConflictException("Invitation already pending for this email");
        }

        FamilyInvitation invitation = FamilyInvitation.builder()
                .family(family)
                .invitedEmail(invitedEmail)
                .invitedBy(admin)
                .token(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING)
                .expiresAt(Instant.now().plus(EXPIRY_DAYS, ChronoUnit.DAYS))
                .build();
        invitation = invitationRepository.save(invitation);

        return invitationMapper.toDto(invitation);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDto> getListPending(final String email) {
        AppUser admin = familyResolver.getAdminUser(email);
        return invitationRepository
                .findByFamilyIdAndStatus(admin.getFamily().getId(), InvitationStatus.PENDING)
                .stream()
                .map(invitationMapper::toDto)
                .toList();
    }

    @Transactional
    public void cancel(final String email, final UUID invitationId) {
        AppUser admin = familyResolver.getAdminUser(email);
        FamilyInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation does not exist"));
        if (!invitation.getFamily().getId().equals(admin.getFamily().getId())) {
            throw new UnauthorizedException("Invitation does not belong to your family");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ConflictException("Only pending invitations can be cancelled");
        }
        invitation.setStatus(InvitationStatus.CANCELLED);
    }

    @Transactional
    public FamilyResponseDto accept(final String email, final String token) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));
        if (user.getFamily() != null) {
            throw new ConflictException("You already belong to a family");
        }

        FamilyInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation does not exist"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ConflictException("Invitation is no longer pending");
        }
        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            throw new ConflictException("Invitation has expired");
        }
        if (!invitation.getInvitedEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("Invitation is for a different email");
        }

        user.setFamily(invitation.getFamily());
        invitation.setStatus(InvitationStatus.ACCEPTED);

        return familyService.getMyFamily(email);
    }
}
