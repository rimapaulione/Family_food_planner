package org.example.planner_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientResponseDto;
import org.example.planner_backend.mapper.IngredientMapper;
import org.example.planner_backend.repository.IngredientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public List<IngredientResponseDto> getAll(String search) {
        return findIngredients(search).stream()
                .map(ingredientMapper::toResponse)
                .toList();
    }

    public List<IngredientDetailResponseDto> getAllWithRecipeCount(String search) {
        String trimmedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        return ingredientRepository.findAllWithRecipeCount(trimmedSearch);
    }

    private List<org.example.planner_backend.model.entity.Ingredient> findIngredients(String search) {
        if (search != null && !search.isBlank()) {
            return ingredientRepository.findByNameLtContainingIgnoreCase(search.trim());
        }
        return ingredientRepository.findAll();
    }
}
