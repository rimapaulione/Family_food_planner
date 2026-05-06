package org.example.planner_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.mealplan.MealPlanResponseDto;
import org.example.planner_backend.dto.mealplan.MealPlanUpdateRequestDto;
import org.example.planner_backend.dto.mealplan.MealPlanWindowResponseDto;
import org.example.planner_backend.dto.mealplan.MealSlotResponseDto;
import org.example.planner_backend.dto.mealplan.MealSlotUpdateRequestDto;
import org.example.planner_backend.service.MealPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/v1/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @GetMapping("/current-and-next")
    public ResponseEntity<MealPlanWindowResponseDto> getCurrentAndNext(
            @AuthenticationPrincipal String email
    ) {
        return ResponseEntity.ok(mealPlanService.getCurrentAndNext(email));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MealPlanResponseDto> updatePlan(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id,
            @Valid @RequestBody MealPlanUpdateRequestDto request
    ) {
        return ResponseEntity.ok(mealPlanService.updatePlan(email, id, request));
    }

    @PatchMapping("/slots/{id}")
    public ResponseEntity<MealSlotResponseDto> updateSlot(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id,
            @Valid @RequestBody MealSlotUpdateRequestDto request
    ) {
        return ResponseEntity.ok(mealPlanService.updateSlot(email, id, request));
    }

    @PostMapping("/{id}/auto-fill")
    public ResponseEntity<MealPlanResponseDto> autoFillPlan(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(mealPlanService.autoFillPlan(email, id));
    }
}
