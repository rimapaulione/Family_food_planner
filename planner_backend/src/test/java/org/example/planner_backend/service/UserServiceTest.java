package org.example.planner_backend.service;

import org.example.planner_backend.dto.user.PasswordUpdateRequestDto;
import org.example.planner_backend.dto.user.UserResponseDto;
import org.example.planner_backend.dto.user.UserUpdateRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.mapper.UserMapper;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.enums.AuthProvider;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UUID USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String EMAIL = "user@example.com";
    private static final String DISPLAY_NAME = "Test User";
    private static final String AVATAR_URL = "https://example.com/avatar.jpg";
    private static final String CURRENT_HASH = "$2a$10$current";
    private static final String NEW_HASH = "$2a$10$newhash";
    private static final String CURRENT_PASSWORD = "oldpassword123";
    private static final String NEW_PASSWORD = "newpassword456";

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AppUser user;
    private UserResponseDto userResponse;

    @BeforeEach
    void setUp() {
        user = AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .displayName(DISPLAY_NAME)
                .passwordHash(CURRENT_HASH)
                .avatarUrl(AVATAR_URL)
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
        userResponse = new UserResponseDto(USER_ID, EMAIL, DISPLAY_NAME, AVATAR_URL, Role.USER);
    }

    // ---------- getUser ----------

    @Test
    void getUser_shouldReturnDtoOnSuccess() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponseDto response = userService.getUser(EMAIL);

        assertThat(response).isEqualTo(userResponse);
    }

    @Test
    void getUser_shouldThrowNotFoundWhenEmailUnknown() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(EMAIL))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User does not exist");
    }

    // ---------- update ----------

    @Test
    void update_shouldUpdateDisplayNameAndAvatarUrl() {
        UserUpdateRequestDto request = new UserUpdateRequestDto("new name", "https://new.url/avatar.png");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        userService.update(EMAIL, request);

        assertThat(user.getDisplayName()).isEqualTo("New name");
        assertThat(user.getAvatarUrl()).isEqualTo("https://new.url/avatar.png");
    }

    @Test
    void update_shouldSetAvatarUrlToNullWhenNullProvided() {
        UserUpdateRequestDto request = new UserUpdateRequestDto("name", null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        userService.update(EMAIL, request);

        assertThat(user.getAvatarUrl()).isNull();
    }

    @Test
    void update_shouldThrowNotFoundWhenUserMissing() {
        UserUpdateRequestDto request = new UserUpdateRequestDto("name", null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(EMAIL, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- changePassword ----------

    @Test
    void changePassword_shouldEncodeAndSetNewPassword() {
        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto(CURRENT_PASSWORD, NEW_PASSWORD);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_PASSWORD, CURRENT_HASH)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD, CURRENT_HASH)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_HASH);

        userService.changePassword(EMAIL, request);

        assertThat(user.getPasswordHash()).isEqualTo(NEW_HASH);
    }

    @Test
    void changePassword_shouldThrowUnauthorizedWhenPasswordHashIsNull() {
        AppUser oauthUser = AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .passwordHash(null)
                .authProvider(AuthProvider.GOOGLE)
                .role(Role.USER)
                .build();
        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto(CURRENT_PASSWORD, NEW_PASSWORD);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(oauthUser));

        assertThatThrownBy(() -> userService.changePassword(EMAIL, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not allowed");

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePassword_shouldThrowUnauthorizedWhenCurrentPasswordWrong() {
        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto("wrong", NEW_PASSWORD);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", CURRENT_HASH)).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(EMAIL, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePassword_shouldThrowConflictWhenNewEqualsCurrent() {
        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto(CURRENT_PASSWORD, CURRENT_PASSWORD);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_PASSWORD, CURRENT_HASH)).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(EMAIL, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("must differ");

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePassword_shouldThrowNotFoundWhenUserMissing() {
        PasswordUpdateRequestDto request = new PasswordUpdateRequestDto(CURRENT_PASSWORD, NEW_PASSWORD);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword(EMAIL, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
