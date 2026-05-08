package org.example.planner_backend.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.family.FamilyMemberRoleRequestDto;
import org.example.planner_backend.dto.family.FamilyRequestDto;
import org.example.planner_backend.dto.family.FamilyResponseDto;
import org.example.planner_backend.dto.family.FamilySettingsRequestDto;
import org.example.planner_backend.service.FamilyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/families")
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    public ResponseEntity<FamilyResponseDto> create(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody FamilyRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(familyService.create(email, request));
    }

    @GetMapping("/me")
    public ResponseEntity<FamilyResponseDto> getMyFamily(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(familyService.getMyFamily(email));
    }

    @PatchMapping("/me")
    public ResponseEntity<FamilyResponseDto> updateMyFamily(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody FamilySettingsRequestDto request
    ) {
        return ResponseEntity.ok(familyService.updateSettings(email, request));
    }

    @PatchMapping("/members/{id}/role")
    public ResponseEntity<FamilyResponseDto> updateMemberRole(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id,
            @Valid @RequestBody FamilyMemberRoleRequestDto request
            ) {
        return ResponseEntity.ok(familyService.updateMemberRole(email, id, request));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<FamilyResponseDto> removeMember(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(familyService.removeMember(email, id));
    }
}
