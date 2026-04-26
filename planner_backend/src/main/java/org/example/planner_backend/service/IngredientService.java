package org.example.planner_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.ingredient.IngredientCheckNameResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientRequestDto;
import org.example.planner_backend.dto.ingredient.IngredientResponseDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.mapper.IngredientMapper;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.repository.IngredientRepository;
import org.example.planner_backend.util.TextUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final FamilyResolver familyResolver;
    private final IngredientMapper ingredientMapper;

    @Transactional(readOnly = true)
    public List<IngredientResponseDto> getAll(final String email, final String search) {
        UUID familyId = familyResolver.getFamilyIdByEmail(email);
        return findIngredients(familyId, search).stream()
                .map(ingredientMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IngredientDetailResponseDto> getAllWithRecipesCount(final String email, final String search) {
        UUID familyId = familyResolver.getFamilyIdByEmail(email);
        String trimmedSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        return ingredientRepository.findAllWithRecipeCount(familyId, trimmedSearch);
    }

    @Transactional(readOnly = true)
    public IngredientCheckNameResponseDto checkName(final String email, final String name) {
        UUID familyId = familyResolver.getFamilyIdByEmail(email);
        String trimmed = name.trim();
        if (trimmed.length() < 2) {
            return new IngredientCheckNameResponseDto(false, null, List.of());
        }
        String prefix = trimmed.length() >= 4 ? trimmed.substring(0, 4) : trimmed;

        List<Ingredient> matches = ingredientRepository
                .findByFamilyIdAndNameLtIgnoreCaseStartingWith(familyId, prefix);

        String existingName = matches.stream()
                .map(Ingredient::getNameLt)
                .filter(nameLt -> nameLt.equalsIgnoreCase(trimmed))
                .findFirst()
                .orElse(null);
        boolean exactMatch = existingName != null;

        List<String> similar = exactMatch ? List.of() :
                matches.stream()
                        .map(Ingredient::getNameLt)
                        .filter(n -> !n.equalsIgnoreCase(trimmed))
                        .limit(5)
                        .toList();

        return new IngredientCheckNameResponseDto(exactMatch, existingName, similar);
    }

    @Transactional
    public IngredientResponseDto create(final String email, final IngredientRequestDto newIngredient) {
        Family family = familyResolver.getFamilyByEmail(email);
        String nameLt = TextUtil.capitalize(newIngredient.nameLt());
        if (ingredientRepository.existsByFamilyIdAndNameLtIgnoreCase(family.getId(), nameLt)) {
            throw new ConflictException("Ingredient '" + nameLt + "' already exists");
        }
        Ingredient ingredient = ingredientRepository.save(
                Ingredient.builder()
                        .nameLt(nameLt)
                        .unit(newIngredient.unit())
                        .family(family)
                        .build()
        );
        return ingredientMapper.toResponse(ingredient);
    }

    @Transactional
    public IngredientResponseDto update(final String email, final UUID id, final IngredientRequestDto updateIngredient) {
        UUID familyId = familyResolver.getFamilyIdByEmail(email);
        Ingredient ingredient = ingredientRepository.findByIdAndFamilyId(id, familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
        String nameLt = TextUtil.capitalize(updateIngredient.nameLt());
        if (!ingredient.getNameLt().equalsIgnoreCase(nameLt)
                && ingredientRepository.existsByFamilyIdAndNameLtIgnoreCase(familyId, nameLt)) {
            throw new ConflictException("Ingredient '" + nameLt + "' already exists");
        }

        ingredient.setNameLt(nameLt);
        ingredient.setUnit(updateIngredient.unit());
        return ingredientMapper.toResponse(ingredientRepository.save(ingredient));
    }

    @Transactional
    public void delete(final String email, final UUID id) {
        UUID familyId = familyResolver.getFamilyIdByEmail(email);
        Ingredient ingredient = ingredientRepository.findByIdAndFamilyId(id, familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
        if (ingredientRepository.countRecipesByIngredientId(ingredient.getId()) > 0) {
            throw new ConflictException("Ingredient is used in recipes and cannot be deleted.");
        }
        ingredientRepository.delete(ingredient);
    }

    private List<Ingredient> findIngredients(final UUID familyId, final String search) {
        if (search != null && !search.isBlank()) {
            return ingredientRepository.findByFamilyIdAndNameLtContainingIgnoreCase(familyId, search.trim());
        }
        return ingredientRepository.findByFamilyId(familyId);
    }
}
