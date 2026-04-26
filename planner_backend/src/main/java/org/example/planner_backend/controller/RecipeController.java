package org.example.planner_backend.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.recipe.RecipeListResponseDto;
import org.example.planner_backend.dto.recipe.RecipeRequestDto;
import org.example.planner_backend.dto.recipe.RecipeResponseDto;
import org.example.planner_backend.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<List<RecipeListResponseDto>> getAll(
            @AuthenticationPrincipal String email,
            @Size(max = 50)
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(recipeService.getAllWithIngredients(email, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> getById(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(recipeService.getById(email, id));
    }

    @PostMapping
    public ResponseEntity<RecipeResponseDto> create(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody RecipeRequestDto newRecipe
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.create(email, newRecipe));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> update(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id,
            @Valid @RequestBody RecipeRequestDto request) {
        return ResponseEntity.ok(recipeService.update(email, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {
        recipeService.delete(email, id);
        return ResponseEntity.noContent().build();
    }
}
