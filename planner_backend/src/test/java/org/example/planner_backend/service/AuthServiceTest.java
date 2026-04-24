package org.example.planner_backend.service;

import org.example.planner_backend.dto.auth.AuthResponseDto;
import org.example.planner_backend.dto.auth.LoginRequestDto;
import org.example.planner_backend.dto.auth.RegisterRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.enums.AuthProvider;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class AuthServiceTest {

    private static final UUID USER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final String EMAIL = "user@example.com";
    private static final String RAW_PASSWORD = "password123";
    private static final String HASHED_PASSWORD = "$2a$10$hashedpassword";
    private static final String DISPLAY_NAME = "Test User";
    private static final String TOKEN = "fake.jwt.token";

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // ---------- register ----------

    @Test
    void register_shouldReturnTokenAndUserInfoWhenSuccess() {
        RegisterRequestDto request = new RegisterRequestDto(EMAIL, RAW_PASSWORD, DISPLAY_NAME);

        when(appUserRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(USER_ID);
            return u;
        });
        when(jwtService.generateToken(EMAIL, Role.USER)).thenReturn(TOKEN);

        AuthResponseDto response = authService.register(request);

        assertThat(response.token()).isEqualTo(TOKEN);
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.displayName()).isEqualTo(DISPLAY_NAME);
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void register_shouldThrowConflictWhenEmailExists() {
        RegisterRequestDto request = new RegisterRequestDto(EMAIL, RAW_PASSWORD, DISPLAY_NAME);

        when(appUserRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists");

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void register_shouldNormalizeEmailAndCapitalizeDisplayName() {
        RegisterRequestDto request = new RegisterRequestDto(
                "  USER@Example.com  ", RAW_PASSWORD, "test user");

        when(appUserRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generateToken(EMAIL, Role.USER)).thenReturn(TOKEN);

        authService.register(request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        AppUser saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getDisplayName()).isEqualTo("Test user");
        assertThat(saved.getPasswordHash()).isEqualTo(HASHED_PASSWORD);
        assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    // ---------- login ----------

    @Test
    void login_shouldReturnTokenWhenCredentialsValid() {
        LoginRequestDto request = new LoginRequestDto(EMAIL, RAW_PASSWORD);
        AppUser user = AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .passwordHash(HASHED_PASSWORD)
                .displayName(DISPLAY_NAME)
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(EMAIL, Role.USER)).thenReturn(TOKEN);

        AuthResponseDto response = authService.login(request);

        assertThat(response.token()).isEqualTo(TOKEN);
        assertThat(response.userId()).isEqualTo(USER_ID);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void login_shouldThrowUnauthorizedWhenEmailUnknown() {
        LoginRequestDto request = new LoginRequestDto(EMAIL, RAW_PASSWORD);

        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void login_shouldThrowUnauthorizedWhenPasswordWrong() {
        LoginRequestDto request = new LoginRequestDto(EMAIL, "wrongpass");
        AppUser user = AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .passwordHash(HASHED_PASSWORD)
                .role(Role.USER)
                .build();

        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", HASHED_PASSWORD)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void login_shouldThrowUnauthorizedWhenPasswordHashIsNull() {
        LoginRequestDto request = new LoginRequestDto(EMAIL, RAW_PASSWORD);
        AppUser oauthUser = AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .passwordHash(null)
                .authProvider(AuthProvider.GOOGLE)
                .role(Role.USER)
                .build();

        when(appUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(oauthUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");

        verify(jwtService, never()).generateToken(any(), any());
    }
}
