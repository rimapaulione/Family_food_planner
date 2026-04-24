package org.example.planner_backend.controller;


import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.user.UserResponseDto;
import org.example.planner_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
}
