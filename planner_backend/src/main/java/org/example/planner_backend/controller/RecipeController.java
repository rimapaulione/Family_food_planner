package org.example.planner_backend.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.recipe.RecipeListResponseDto;
import org.example.planner_backend.dto.recipe.RecipeRequestDto;
import org.example.planner_backend.dto.recipe.RecipeResponseDto;
import org.example.planner_backend.service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<List<RecipeListResponseDto>> getRecipes(
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(recipeService.getAll(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> getRecipeById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(recipeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<RecipeResponseDto> createRecipe(
            @Valid @RequestBody RecipeRequestDto newRecipe
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.create(newRecipe));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> update(@PathVariable UUID id,
                                                    @Valid @RequestBody RecipeRequestDto request) {
        return ResponseEntity.ok(recipeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recipeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
