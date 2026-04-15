package org.example.planner_backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.ingredient.IngredientCheckNameResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientRequestDto;
import org.example.planner_backend.dto.ingredient.IngredientResponseDto;
import org.example.planner_backend.service.IngredientService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
@Validated
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public ResponseEntity<List<IngredientResponseDto>> getAll(
            @Size(min = 1, max = 50)
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ingredientService.getAll(search));
    }

    @GetMapping("/with-recipes-count")
    public ResponseEntity<List<IngredientDetailResponseDto>> getAllWithDetails(
            @Size(min = 1, max = 50)
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ingredientService.getAllWithRecipesCount(search));
    }

    @GetMapping("/check")
    public ResponseEntity<IngredientCheckNameResponseDto> checkName(
            @RequestParam @NotBlank @Size(min = 1, max = 100) String name) {
        return ResponseEntity.ok(ingredientService.checkName(name));
    }

    @PostMapping()
    public ResponseEntity<IngredientResponseDto> create(
            @Valid @RequestBody IngredientRequestDto newIngredient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredientService.create(newIngredient));
    }


    @PutMapping("{id}")
    public ResponseEntity<IngredientResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody IngredientRequestDto request
    ) {
        return ResponseEntity.ok(ingredientService.update(id, request));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ingredientService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
