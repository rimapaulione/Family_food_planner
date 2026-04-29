package org.example.planner_backend.service;

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
import org.example.planner_backend.model.entity.MealServings;
import org.example.planner_backend.model.enums.AuthProvider;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.repository.AppUserRepository;
import org.example.planner_backend.repository.FamilyRepository;
import org.example.planner_backend.repository.IngredientRepository;
import org.example.planner_backend.repository.IngredientTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FAMILY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String EMAIL = "user@example.com";
    private static final String DISPLAY_NAME = "Test User";
    private static final String FAMILY_NAME = "Smith Family";

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientTemplateRepository ingredientTemplateRepository;

    @Mock
    private FamilyResolver familyResolver;

    @Mock
    private FamilyMapper familyMapper;

    @InjectMocks
    private FamilyService familyService;

    private AppUser user;
    private Family family;

    @BeforeEach
    void setUp() {
        user = AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .displayName(DISPLAY_NAME)
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
        family = Family.builder()
                .id(FAMILY_ID)
                .name(FAMILY_NAME)
                .createdBy(user)
                .isSetupCompleted(false)
                .shoppingDay(DayOfWeek.SUNDAY)
                .defaultWeekdayServings(new MealServings(3, null, 4))
                .defaultWeekendServings(new MealServings(4, 4, 4))
                .build();
    }

    // ---------- create ----------

    @Test
    void create_shouldCreateFamilyAndAssignUserAsAdmin() {
        FamilyRequestDto request = new FamilyRequestDto(FAMILY_NAME);
        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(familyRepository.save(any(Family.class))).thenAnswer(i -> {
            Family f = i.getArgument(0);
            f.setId(FAMILY_ID);
            return f;
        });
        when(appUserRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of(user));
        when(familyMapper.toMembers(any())).thenReturn(List.of());

        FamilyResponseDto response = familyService.create(EMAIL, request);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getFamily()).isNotNull();
        assertThat(response.name()).isEqualTo(FAMILY_NAME);
    }

    @Test
    void create_shouldThrowNotFoundWhenUserMissing() {
        FamilyRequestDto request = new FamilyRequestDto(FAMILY_NAME);
        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> familyService.create(EMAIL, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(familyRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowConflictWhenUserAlreadyHasFamily() {
        FamilyRequestDto request = new FamilyRequestDto(FAMILY_NAME);
        user.setFamily(family);
        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> familyService.create(EMAIL, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already has a family");

        verify(familyRepository, never()).save(any());
    }

    // ---------- getMyFamily ----------

    @Test
    void getMyFamily_shouldReturnDtoOnSuccess() {
        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(appUserRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of(user));
        when(familyMapper.toMembers(any())).thenReturn(List.of());

        FamilyResponseDto response = familyService.getMyFamily(EMAIL);

        assertThat(response.id()).isEqualTo(FAMILY_ID);
        assertThat(response.name()).isEqualTo(FAMILY_NAME);
    }

    @Test
    void getMyFamily_shouldThrowNotFoundWhenResolverFails() {
        when(familyResolver.getFamilyByEmail(EMAIL))
                .thenThrow(new ResourceNotFoundException("User has no family"));

        assertThatThrownBy(() -> familyService.getMyFamily(EMAIL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no family");
    }

    // ---------- updateSettings ----------

    @Test
    void updateSettings_shouldUpdateAllFields() {
        user.setFamily(family);
        user.setRole(Role.ADMIN);
        FamilySettingsRequestDto request = new FamilySettingsRequestDto(
                "New Name",
                DayOfWeek.MONDAY,
                new MealServings(5, 5, 5),
                new MealServings(6, 6, 6),
                true
        );
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(user);
        when(appUserRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of(user));
        when(familyMapper.toMembers(any())).thenReturn(List.of());

        familyService.updateSettings(EMAIL, request);

        assertThat(family.getName()).isEqualTo("New Name");
        assertThat(family.getShoppingDay()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(family.getDefaultWeekdayServings()).isEqualTo(new MealServings(5, 5, 5));
        assertThat(family.getDefaultWeekendServings()).isEqualTo(new MealServings(6, 6, 6));
        assertThat(family.isSetupCompleted()).isTrue();
    }

    @Test
    void updateSettings_shouldThrowNotFoundWhenUserHasNoFamily() {
        FamilySettingsRequestDto request = new FamilySettingsRequestDto(
                "X", DayOfWeek.MONDAY, new MealServings(3, null, 4), new MealServings(4, 4, 4), true
        );
        when(familyResolver.getAdminUser(EMAIL))
                .thenThrow(new ResourceNotFoundException("User has no family"));

        assertThatThrownBy(() -> familyService.updateSettings(EMAIL, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no family");
    }

    @Test
    void updateSettings_shouldThrowUnauthorizedWhenUserIsNotAdmin() {
        FamilySettingsRequestDto request = new FamilySettingsRequestDto(
                "X", DayOfWeek.MONDAY, new MealServings(3, null, 4), new MealServings(4, 4, 4), true
        );
        when(familyResolver.getAdminUser(EMAIL))
                .thenThrow(new UnauthorizedException("Admin role required"));

        assertThatThrownBy(() -> familyService.updateSettings(EMAIL, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Admin");
    }

    // ---------- updateMemberRole ----------

    private static final UUID MEMBER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_FAMILY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private AppUser adminUser() {
        AppUser admin = AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .displayName(DISPLAY_NAME)
                .authProvider(AuthProvider.LOCAL)
                .role(Role.ADMIN)
                .family(family)
                .build();
        return admin;
    }

    private AppUser memberInSameFamily(Role role) {
        return AppUser.builder()
                .id(MEMBER_ID)
                .email("member@example.com")
                .displayName("Member")
                .authProvider(AuthProvider.LOCAL)
                .role(role)
                .family(family)
                .build();
    }

    @Test
    void updateMemberRole_shouldPromoteUserToAdmin() {
        AppUser admin = adminUser();
        AppUser member = memberInSameFamily(Role.USER);
        FamilyMemberRoleRequestDto request = new FamilyMemberRoleRequestDto(Role.ADMIN);
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(admin);
        when(appUserRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));
        when(appUserRepository.countByFamilyIdAndRole(FAMILY_ID, Role.ADMIN)).thenReturn(1L);
        when(appUserRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of(admin, member));
        when(familyMapper.toMembers(any())).thenReturn(List.of());

        familyService.updateMemberRole(EMAIL, MEMBER_ID, request);

        assertThat(member.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void updateMemberRole_shouldDemoteAdminToUserWhenMultipleAdminsExist() {
        AppUser admin = adminUser();
        AppUser member = memberInSameFamily(Role.ADMIN);
        FamilyMemberRoleRequestDto request = new FamilyMemberRoleRequestDto(Role.USER);
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(admin);
        when(appUserRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));
        when(appUserRepository.countByFamilyIdAndRole(FAMILY_ID, Role.ADMIN)).thenReturn(2L);
        when(appUserRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of(admin, member));
        when(familyMapper.toMembers(any())).thenReturn(List.of());

        familyService.updateMemberRole(EMAIL, MEMBER_ID, request);

        assertThat(member.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void updateMemberRole_shouldThrowNotFoundWhenMemberDoesNotExist() {
        FamilyMemberRoleRequestDto request = new FamilyMemberRoleRequestDto(Role.ADMIN);
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(adminUser());
        when(appUserRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> familyService.updateMemberRole(EMAIL, MEMBER_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateMemberRole_shouldThrowUnauthorizedWhenMemberInDifferentFamily() {
        Family otherFamily = Family.builder().id(OTHER_FAMILY_ID).name("Other").build();
        AppUser member = AppUser.builder()
                .id(MEMBER_ID)
                .email("other@example.com")
                .displayName("Other")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .family(otherFamily)
                .build();
        FamilyMemberRoleRequestDto request = new FamilyMemberRoleRequestDto(Role.ADMIN);
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(adminUser());
        when(appUserRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> familyService.updateMemberRole(EMAIL, MEMBER_ID, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not in your family");
    }

    @Test
    void updateMemberRole_shouldThrowUnauthorizedWhenMemberHasNoFamily() {
        AppUser member = AppUser.builder()
                .id(MEMBER_ID)
                .email("orphan@example.com")
                .displayName("Orphan")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
        FamilyMemberRoleRequestDto request = new FamilyMemberRoleRequestDto(Role.ADMIN);
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(adminUser());
        when(appUserRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> familyService.updateMemberRole(EMAIL, MEMBER_ID, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not in your family");
    }

    @Test
    void updateMemberRole_shouldThrowConflictWhenChangingOwnRole() {
        AppUser admin = adminUser();
        FamilyMemberRoleRequestDto request = new FamilyMemberRoleRequestDto(Role.USER);
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(admin);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> familyService.updateMemberRole(EMAIL, USER_ID, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("your own role");
    }

    @Test
    void updateMemberRole_shouldThrowConflictWhenDemotingLastAdmin() {
        AppUser admin = adminUser();
        AppUser member = memberInSameFamily(Role.ADMIN);
        FamilyMemberRoleRequestDto request = new FamilyMemberRoleRequestDto(Role.USER);
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(admin);
        when(appUserRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));
        when(appUserRepository.countByFamilyIdAndRole(FAMILY_ID, Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> familyService.updateMemberRole(EMAIL, MEMBER_ID, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("at least one admin");

        assertThat(member.getRole()).isEqualTo(Role.ADMIN);
    }
}
