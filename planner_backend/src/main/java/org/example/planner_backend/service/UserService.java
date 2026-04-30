package org.example.planner_backend.service;


import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.user.PasswordUpdateRequestDto;
import org.example.planner_backend.dto.user.UserResponseDto;
import org.example.planner_backend.dto.user.UserUpdateRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.mapper.UserMapper;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.util.TextUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final FamilyResolver familyResolver;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    public UserResponseDto getUser(final String email) {
        AppUser user = familyResolver.getUserByEmail(email);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponseDto update(final String email, final UserUpdateRequestDto request) {
        AppUser user = familyResolver.getUserByEmail(email);

        user.setDisplayName(TextUtil.capitalize(request.displayName()));
        user.setAvatarUrl(request.avatarUrl());

        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(final String email, final PasswordUpdateRequestDto request) {
        AppUser user = familyResolver.getUserByEmail(email);

        if (user.getPasswordHash() == null) {
            throw new UnauthorizedException("Password change not allowed for this account");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ConflictException("New password must differ from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }
}
