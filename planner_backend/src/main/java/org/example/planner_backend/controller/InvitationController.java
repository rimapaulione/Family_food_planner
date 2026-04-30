package org.example.planner_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.family.FamilyResponseDto;
import org.example.planner_backend.dto.invitation.InvitationPublicDto;
import org.example.planner_backend.dto.invitation.InvitationRequestDto;
import org.example.planner_backend.dto.invitation.InvitationResponseDto;
import org.example.planner_backend.service.InvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping
    public ResponseEntity<InvitationResponseDto> invite(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody InvitationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.invite(email, request));
    }

    @GetMapping("/family")
    public List<InvitationResponseDto> getListPending(@AuthenticationPrincipal String email) {
        return invitationService.getListPending(email);
    }

    @GetMapping("/public/{token}")
    public InvitationPublicDto getPublic(@PathVariable String token) {
        return invitationService.getPublicByToken(token);
    }

    @PostMapping("/accept/{token}")
    public FamilyResponseDto accept(
            @AuthenticationPrincipal String email,
            @PathVariable String token) {
        return invitationService.accept(email, token);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {
        invitationService.cancel(email, id);
        return ResponseEntity.noContent().build();
    }
}
