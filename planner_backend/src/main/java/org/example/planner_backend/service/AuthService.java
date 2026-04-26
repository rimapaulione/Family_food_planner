package org.example.planner_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.auth.AuthResponseDto;
import org.example.planner_backend.dto.auth.LoginRequestDto;
import org.example.planner_backend.dto.auth.RegisterRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.enums.AuthProvider;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.repository.AppUserRepository;
import org.example.planner_backend.util.TextUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDto register(final RegisterRequestDto request) {
        String email = request.email().trim().toLowerCase();
        if (appUserRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }

        AppUser user = AppUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(TextUtil.capitalize(request.displayName()))
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        user = appUserRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return toAuthResponse(user, token);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(final LoginRequestDto request) {
        String email = request.email().trim().toLowerCase();

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return toAuthResponse(user, token);
    }


    private AuthResponseDto toAuthResponse(final AppUser user, final String token) {
        return new AuthResponseDto(
                token,
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getFamily() != null ? user.getFamily().getId() : null
        );
    }
}
