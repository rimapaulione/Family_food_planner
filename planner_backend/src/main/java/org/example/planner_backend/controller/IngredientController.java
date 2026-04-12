package org.example.planner_backend.controller;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientResponseDto;
import org.example.planner_backend.service.IngredientService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
@RequiredArgsConstructor
@Validated
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public ResponseEntity<List<IngredientResponseDto>> getIngredients(
            @Size(min = 2, max = 50)
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ingredientService.getAll(search));
    }

    @GetMapping("/with-recipes-count")
    public ResponseEntity<List<IngredientDetailResponseDto>> getIngredientsWithDetails(
            @Size(min = 2, max = 50)
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ingredientService.getAllWithRecipesCount(search));
    }
}
