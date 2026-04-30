package org.example.planner_backend.service;

import org.example.planner_backend.dto.invitation.InvitationPublicDto;
import org.example.planner_backend.dto.invitation.InvitationRequestDto;
import org.example.planner_backend.dto.invitation.InvitationResponseDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.mapper.InvitationMapper;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.FamilyInvitation;
import org.example.planner_backend.model.enums.AuthProvider;
import org.example.planner_backend.model.enums.InvitationStatus;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.repository.AppUserRepository;
import org.example.planner_backend.repository.InvitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    private static final UUID INVITER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID INVITEE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID FAMILY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_FAMILY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID INVITATION_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String INVITER_EMAIL = "admin@example.com";
    private static final String INVITED_EMAIL = "invitee@example.com";
    private static final String TOKEN = "abc-123-token";

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private FamilyResolver familyResolver;
    @Mock
    private FamilyService familyService;
    @Mock
    private InvitationMapper invitationMapper;

    @InjectMocks
    private InvitationService invitationService;

    private AppUser inviter;
    private AppUser invitee;
    private Family family;

    @BeforeEach
    void setUp() {
        family = Family.builder()
                .id(FAMILY_ID)
                .name("Smith Family")
                .shoppingDay(DayOfWeek.SUNDAY)
                .build();
        inviter = AppUser.builder()
                .id(INVITER_ID)
                .email(INVITER_EMAIL)
                .displayName("Admin")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.ADMIN)
                .family(family)
                .build();
        invitee = AppUser.builder()
                .id(INVITEE_ID)
                .email(INVITED_EMAIL)
                .displayName("Invitee")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
    }

    // ---------- invite ----------

    @Test
    void invite_shouldCreateInvitationSuccessfully() {
        InvitationRequestDto request = new InvitationRequestDto(INVITED_EMAIL);
        when(familyResolver.getAdminUser(INVITER_EMAIL)).thenReturn(inviter);
        when(appUserRepository.findByEmail(INVITED_EMAIL)).thenReturn(Optional.empty());
        when(invitationRepository.existsByFamilyIdAndInvitedEmailAndStatus(
                FAMILY_ID, INVITED_EMAIL, InvitationStatus.PENDING)).thenReturn(false);
        when(invitationRepository.save(any(FamilyInvitation.class))).thenAnswer(i -> i.getArgument(0));
        when(invitationMapper.toDto(any())).thenReturn(
                new InvitationResponseDto(null, INVITED_EMAIL, InvitationStatus.PENDING, null, null, null));

        InvitationResponseDto response = invitationService.invite(INVITER_EMAIL, request);

        assertThat(response.invitedEmail()).isEqualTo(INVITED_EMAIL);
        verify(invitationRepository).save(any(FamilyInvitation.class));
    }

    @Test
    void invite_shouldThrowConflictWhenSelfInvite() {
        InvitationRequestDto request = new InvitationRequestDto(INVITER_EMAIL);
        when(familyResolver.getAdminUser(INVITER_EMAIL)).thenReturn(inviter);

        assertThatThrownBy(() -> invitationService.invite(INVITER_EMAIL, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("yourself");

        verify(invitationRepository, never()).save(any());
    }

    @Test
    void invite_shouldThrowConflictWhenInviteeAlreadyHasFamily() {
        invitee.setFamily(Family.builder().id(OTHER_FAMILY_ID).name("Other").build());
        InvitationRequestDto request = new InvitationRequestDto(INVITED_EMAIL);
        when(familyResolver.getAdminUser(INVITER_EMAIL)).thenReturn(inviter);
        when(appUserRepository.findByEmail(INVITED_EMAIL)).thenReturn(Optional.of(invitee));

        assertThatThrownBy(() -> invitationService.invite(INVITER_EMAIL, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already belongs");

        verify(invitationRepository, never()).save(any());
    }

    @Test
    void invite_shouldThrowConflictWhenInvitationAlreadyPending() {
        InvitationRequestDto request = new InvitationRequestDto(INVITED_EMAIL);
        when(familyResolver.getAdminUser(INVITER_EMAIL)).thenReturn(inviter);
        when(appUserRepository.findByEmail(INVITED_EMAIL)).thenReturn(Optional.empty());
        when(invitationRepository.existsByFamilyIdAndInvitedEmailAndStatus(
                FAMILY_ID, INVITED_EMAIL, InvitationStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> invitationService.invite(INVITER_EMAIL, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already pending");

        verify(invitationRepository, never()).save(any());
    }

    // ---------- getPublicByToken ----------

    @Test
    void getPublicByToken_shouldReturnDto() {
        FamilyInvitation invitation = FamilyInvitation.builder()
                .family(family).invitedEmail(INVITED_EMAIL).token(TOKEN)
                .status(InvitationStatus.PENDING).build();
        when(invitationRepository.findByToken(TOKEN)).thenReturn(Optional.of(invitation));
        when(invitationMapper.toPublicDto(invitation)).thenReturn(
                new InvitationPublicDto("Smith Family", INVITED_EMAIL, InvitationStatus.PENDING));

        InvitationPublicDto response = invitationService.getPublicByToken(TOKEN);

        assertThat(response.familyName()).isEqualTo("Smith Family");
        assertThat(response.invitedEmail()).isEqualTo(INVITED_EMAIL);
    }

    @Test
    void getPublicByToken_shouldThrowNotFoundWhenTokenInvalid() {
        when(invitationRepository.findByToken(TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.getPublicByToken(TOKEN))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- cancel ----------

    @Test
    void cancel_shouldMarkInvitationCancelled() {
        FamilyInvitation invitation = FamilyInvitation.builder()
                .id(INVITATION_ID).family(family).invitedEmail(INVITED_EMAIL)
                .token(TOKEN).status(InvitationStatus.PENDING).build();
        when(familyResolver.getAdminUser(INVITER_EMAIL)).thenReturn(inviter);
        when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(invitation));

        invitationService.cancel(INVITER_EMAIL, INVITATION_ID);

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.CANCELLED);
    }

    @Test
    void cancel_shouldThrowUnauthorizedWhenInvitationFromOtherFamily() {
        Family otherFamily = Family.builder().id(OTHER_FAMILY_ID).name("Other").build();
        FamilyInvitation invitation = FamilyInvitation.builder()
                .id(INVITATION_ID).family(otherFamily).invitedEmail(INVITED_EMAIL)
                .token(TOKEN).status(InvitationStatus.PENDING).build();
        when(familyResolver.getAdminUser(INVITER_EMAIL)).thenReturn(inviter);
        when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.cancel(INVITER_EMAIL, INVITATION_ID))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("your family");

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    void cancel_shouldThrowConflictWhenInvitationNotPending() {
        FamilyInvitation invitation = FamilyInvitation.builder()
                .id(INVITATION_ID).family(family).invitedEmail(INVITED_EMAIL)
                .token(TOKEN).status(InvitationStatus.ACCEPTED).build();
        when(familyResolver.getAdminUser(INVITER_EMAIL)).thenReturn(inviter);
        when(invitationRepository.findById(INVITATION_ID)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.cancel(INVITER_EMAIL, INVITATION_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("pending");
    }

    // ---------- accept ----------

    @Test
    void accept_shouldAcceptValidInvitation() {
        FamilyInvitation invitation = FamilyInvitation.builder()
                .id(INVITATION_ID).family(family).invitedEmail(INVITED_EMAIL)
                .token(TOKEN).status(InvitationStatus.PENDING)
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
        when(familyResolver.getUserByEmail(INVITED_EMAIL)).thenReturn(invitee);
        when(invitationRepository.findByToken(TOKEN)).thenReturn(Optional.of(invitation));

        invitationService.accept(INVITED_EMAIL, TOKEN);

        assertThat(invitee.getFamily()).isEqualTo(family);
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(familyService).getMyFamily(INVITED_EMAIL);
    }

    @Test
    void accept_shouldThrowConflictWhenUserAlreadyHasFamily() {
        invitee.setFamily(Family.builder().id(OTHER_FAMILY_ID).name("Other").build());
        when(familyResolver.getUserByEmail(INVITED_EMAIL)).thenReturn(invitee);

        assertThatThrownBy(() -> invitationService.accept(INVITED_EMAIL, TOKEN))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already belong");
    }

    @Test
    void accept_shouldThrowConflictWhenInvitationExpired() {
        FamilyInvitation invitation = FamilyInvitation.builder()
                .id(INVITATION_ID).family(family).invitedEmail(INVITED_EMAIL)
                .token(TOKEN).status(InvitationStatus.PENDING)
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(familyResolver.getUserByEmail(INVITED_EMAIL)).thenReturn(invitee);
        when(invitationRepository.findByToken(TOKEN)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.accept(INVITED_EMAIL, TOKEN))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("expired");

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
    }

    @Test
    void accept_shouldThrowUnauthorizedWhenEmailMismatch() {
        FamilyInvitation invitation = FamilyInvitation.builder()
                .id(INVITATION_ID).family(family).invitedEmail("other@example.com")
                .token(TOKEN).status(InvitationStatus.PENDING)
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
        when(familyResolver.getUserByEmail(INVITED_EMAIL)).thenReturn(invitee);
        when(invitationRepository.findByToken(TOKEN)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.accept(INVITED_EMAIL, TOKEN))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("different email");

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
    }
}
