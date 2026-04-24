package org.example.planner_backend.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.user.PasswordUpdateRequestDto;
import org.example.planner_backend.dto.user.UserResponseDto;
import org.example.planner_backend.dto.user.UserUpdateRequestDto;
import org.example.planner_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getProfile(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(userService.getUser(email));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        return ResponseEntity.ok(userService.update(email, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody PasswordUpdateRequestDto request
    ) {
        userService.changePassword(email, request);
        return ResponseEntity.noContent().build();
    }
}
