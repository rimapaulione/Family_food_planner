package org.example.planner_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.ai.GenerateRecipeRequestDto;
import org.example.planner_backend.dto.ai.GenerateRecipeResponseDto;
import org.example.planner_backend.service.OpenAiRecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai/recipes")
public class AiRecipeController {

    private final OpenAiRecipeService aiService;

    @PostMapping("/generate")
    public ResponseEntity<GenerateRecipeResponseDto> generate(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody GenerateRecipeRequestDto request
    ) {
        return ResponseEntity.ok(aiService.generate(email, request));
    }
}
