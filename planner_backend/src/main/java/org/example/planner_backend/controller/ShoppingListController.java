package org.example.planner_backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.shopping.CreateManualItemRequestDto;
import org.example.planner_backend.dto.shopping.MarkBoughtPlanRequestDto;
import org.example.planner_backend.dto.shopping.ShoppingItemManualDto;
import org.example.planner_backend.dto.shopping.ShoppingListResponseDto;
import org.example.planner_backend.dto.shopping.UpdateManualItemBoughtRequestDto;
import org.example.planner_backend.service.ShoppingListService;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-lists")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping("/family")
    public ResponseEntity<ShoppingListResponseDto> getForFamily(
            @AuthenticationPrincipal String email,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        return ResponseEntity.ok(shoppingListService.getForFamily(email, weekStart));
    }

    @PostMapping("/checks")
    public ResponseEntity<Void> markBought(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody MarkBoughtPlanRequestDto request
    ) {
        shoppingListService.markBought(email, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/checks")
    public ResponseEntity<Void> markUnbought(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody MarkBoughtPlanRequestDto request
    ) {
        shoppingListService.markUnbought(email, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/manual")
    public ResponseEntity<ShoppingItemManualDto> createManual(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody CreateManualItemRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.createManualItem(email, request));
    }

    @PatchMapping("/manual/{id}")
    public ResponseEntity<Void> updateManualBought(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateManualItemBoughtRequestDto request
    ) {
        shoppingListService.updateManualItemBought(email, id, request);
        return ResponseEntity.noContent().build();
    }
}
