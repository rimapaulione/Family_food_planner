package org.example.planner_backend.service;


import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.recipe.RecipeListResponseDto;
import org.example.planner_backend.dto.recipe.RecipeRequestDto;
import org.example.planner_backend.dto.recipe.RecipeResponseDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.mapper.CategoryMapper;
import org.example.planner_backend.mapper.RecipeMapper;
import org.example.planner_backend.mapper.TagMapper;
import org.example.planner_backend.model.entity.Category;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.model.entity.Recipe;
import org.example.planner_backend.model.entity.RecipeIngredient;
import org.example.planner_backend.model.entity.Tag;
import org.example.planner_backend.repository.CategoryRepository;
import org.example.planner_backend.repository.IngredientRepository;
import org.example.planner_backend.repository.RecipeRepository;
import org.example.planner_backend.repository.TagRepository;
import org.example.planner_backend.util.TextUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final IngredientRepository ingredientRepository;
    private final FamilyResolver familyResolver;
    private final RecipeMapper recipeMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Transactional(readOnly = true)
    public List<RecipeListResponseDto> getAllWithIngredients(final String email, final String search) {
        UUID familyId = familyResolver.getFamilyIdByEmail(email);
        List<Recipe> recipes = (search != null && !search.isBlank())
                ? recipeRepository.findByFamilyIdAndNameContainingIgnoreCase(familyId, search.trim())
                : recipeRepository.findByFamilyId(familyId);

        return recipes.stream().map(r -> new RecipeListResponseDto(
                r.getId(),
                r.getName(),
                categoryMapper.toResponse(r.getCategory()),
                r.getDefaultServing(),
                r.getCookingTimeMinutes(),
                r.getTags().stream().map(tagMapper::toResponse).collect(Collectors.toSet()),
                r.getIsFavorite(),
                r.getIngredients().size()
        )).toList();
    }

    @Transactional(readOnly = true)
    public RecipeResponseDto getById(final String email, final UUID id) {
        UUID familyId = familyResolver.getFamilyIdByEmail(email);
        Recipe recipe = recipeRepository.findByIdAndFamilyId(id, familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));

        return recipeMapper.toResponse(recipe);
    }

    @Transactional
    public RecipeResponseDto create(final String email, final RecipeRequestDto request) {
        Family family = familyResolver.getFamilyByEmail(email);
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Recipe recipe = Recipe.builder()
                .name(TextUtil.capitalize(request.name()))
                .category(category)
                .defaultServing(request.defaultServing())
                .cookingTimeMinutes(request.cookingTimeMinutes())
                .isFavorite(request.isFavorite() != null ? request.isFavorite() : false)
                .notes(request.notes())
                .family(family)
                .build();

        this.setTags(recipe, request.tagIds());
        this.setLeftoverRecipe(recipe, family.getId(), request.leftoverRecipeId());
        this.setIngredients(recipe, family.getId(), request.ingredients());

        return recipeMapper.toResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public RecipeResponseDto update(final String email, final UUID id, final RecipeRequestDto request) {
        UUID familyId = familyResolver.getFamilyIdByEmail(email);
        Recipe recipe = recipeRepository.findByIdAndFamilyId(id, familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        recipe.setName(TextUtil.capitalize(request.name()));
        recipe.setCategory(category);
        recipe.setDefaultServing(request.defaultServing());
        recipe.setCookingTimeMinutes(request.cookingTimeMinutes());
        recipe.setIsFavorite(request.isFavorite() != null ? request.isFavorite() : false);
        recipe.setNotes(request.notes());

        this.setTags(recipe, request.tagIds());
        this.setLeftoverRecipe(recipe, familyId, request.leftoverRecipeId());
        this.setIngredients(recipe, familyId, request.ingredients());

        return recipeMapper.toResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public void delete(final String email, final UUID id) {
        UUID familyId = familyResolver.getFamilyIdByEmail(email);
        Recipe recipe = recipeRepository.findByIdAndFamilyId(id, familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));
        recipeRepository.delete(recipe);
    }

    private void setTags(final Recipe recipe, final Set<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Tag> foundTags = tagRepository.findByIdIn(tagIds);
            if (foundTags.size() != tagIds.size()) {
                throw new ResourceNotFoundException("Some tags not found");
            }
            recipe.setTags(foundTags);
        } else {
            recipe.getTags().clear();
        }
    }

    private void setLeftoverRecipe(final Recipe recipe, final UUID familyId, final UUID leftoverRecipeId) {
        if (leftoverRecipeId != null) {
            Recipe leftover = recipeRepository.findByIdAndFamilyId(leftoverRecipeId, familyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Leftover recipe not found"));
            recipe.setLeftoverRecipe(leftover);
        } else {
            recipe.setLeftoverRecipe(null);
        }
    }

    private void setIngredients(final Recipe recipe, final UUID familyId,
                                final List<RecipeRequestDto.RecipeIngredientRequestDto> ingredients) {
        if (ingredients != null) {
            Set<UUID> seenIds = new HashSet<>();
            for (RecipeRequestDto.RecipeIngredientRequestDto ri : ingredients) {
                if (!seenIds.add(ri.ingredientId())) {
                    throw new ConflictException("Same ingredient added more than once");
                }
            }
        }
        recipe.getIngredients().clear();
        recipeRepository.flush();
        if (ingredients != null && !ingredients.isEmpty()) {
            List<RecipeIngredient> recipeIngredients = ingredients.stream()
                    .map(ri -> {
                        Ingredient ingredient = ingredientRepository.findByIdAndFamilyId(ri.ingredientId(), familyId)
                                .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found: " + ri.ingredientId()));
                        return RecipeIngredient.builder()
                                .recipe(recipe)
                                .ingredient(ingredient)
                                .quantity(ri.quantity())
                                .build();
                    }).toList();
            recipe.getIngredients().addAll(recipeIngredients);
        }
    }
}
