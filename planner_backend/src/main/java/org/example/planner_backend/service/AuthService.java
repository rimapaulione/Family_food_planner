package org.example.planner_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.auth.AuthResponseDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.enums.AuthProvider;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public AuthResponseDto register(String email, String password, String displayName) {
        if (appUserRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }

        AppUser user = AppUser.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .displayName(displayName)
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        user = appUserRepository.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return toAuthResponse(user, token);
    }

    public AuthResponseDto login(String email, String password) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return toAuthResponse(user, token);
    }


    private AuthResponseDto toAuthResponse(AppUser user, String token) {
        return new AuthResponseDto(
                token,
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }
}
