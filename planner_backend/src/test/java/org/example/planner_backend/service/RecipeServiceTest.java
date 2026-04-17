package org.example.planner_backend.service;

import org.example.planner_backend.dto.category.CategoryResponseDto;
import org.example.planner_backend.dto.recipe.RecipeListResponseDto;
import org.example.planner_backend.dto.recipe.RecipeRequestDto;
import org.example.planner_backend.dto.recipe.RecipeRequestDto.RecipeIngredientRequestDto;
import org.example.planner_backend.dto.recipe.RecipeResponseDto;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.mapper.CategoryMapper;
import org.example.planner_backend.mapper.RecipeMapper;
import org.example.planner_backend.mapper.TagMapper;
import org.example.planner_backend.model.entity.Category;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.model.entity.Recipe;
import org.example.planner_backend.model.entity.Tag;
import org.example.planner_backend.model.enums.Unit;
import org.example.planner_backend.repository.CategoryRepository;
import org.example.planner_backend.repository.IngredientRepository;
import org.example.planner_backend.repository.RecipeRepository;
import org.example.planner_backend.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    private static final UUID RECIPE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID INGREDIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID LEFTOVER_RECIPE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Long CATEGORY_ID = 10L;
    private static final Long TAG_ID = 20L;
    private static final String RECIPE_NAME = "Blynai";
    private static final String CATEGORY_NAME = "Pusryčiai";
    private static final String TAG_NAME = "Greitas";
    private static final String INGREDIENT_NAME = "Pienas";

    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private IngredientRepository ingredientRepository;
    @Mock
    private RecipeMapper recipeMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private RecipeService recipeService;

    private Category category;
    private Tag tag;
    private Ingredient ingredient;
    private Recipe recipe;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(CATEGORY_ID);
        category.setName(CATEGORY_NAME);

        tag = Tag.builder().id(TAG_ID).name(TAG_NAME).build();

        ingredient = Ingredient.builder()
                .id(INGREDIENT_ID)
                .nameLt(INGREDIENT_NAME)
                .unit(Unit.ML)
                .build();

        recipe = Recipe.builder()
                .id(RECIPE_ID)
                .name(RECIPE_NAME)
                .category(category)
                .defaultServing((short) 4)
                .cookingTimeMinutes((short) 30)
                .isFavorite(false)
                .notes("notes")
                .build();
    }

    private RecipeRequestDto validRequest() {
        return new RecipeRequestDto(
                RECIPE_NAME,
                CATEGORY_ID,
                (short) 4,
                (short) 30,
                Set.of(TAG_ID),
                null,
                false,
                "notes",
                List.of(new RecipeIngredientRequestDto(INGREDIENT_ID, new BigDecimal("100")))
        );
    }

    private RecipeResponseDto aRecipeResponse() {
        return new RecipeResponseDto(
                RECIPE_ID, RECIPE_NAME, null, (short) 4, (short) 30,
                Set.of(), List.of(), null, false, "notes", null, null
        );
    }

    private CategoryResponseDto aCategoryResponse() {
        return new CategoryResponseDto(CATEGORY_ID, CATEGORY_NAME);
    }

    // ---------- getAllWithIngredients ----------

    @Test
    void test_shouldReturnAllRecipesWhenSearchIsBlank() {
        CategoryResponseDto categoryResponse = aCategoryResponse();
        when(recipeRepository.findAllWithTagsAndIngredients()).thenReturn(List.of(recipe));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        List<RecipeListResponseDto> result = recipeService.getAllWithIngredients("  ");

        assertThat(result).hasSize(1);
        RecipeListResponseDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(RECIPE_ID);
        assertThat(dto.name()).isEqualTo(RECIPE_NAME);
        assertThat(dto.category()).isEqualTo(categoryResponse);
        assertThat(dto.defaultServing()).isEqualTo((short) 4);
        assertThat(dto.cookingTimeMinutes()).isEqualTo((short) 30);
        assertThat(dto.tags()).isEmpty();
        assertThat(dto.isFavorite()).isFalse();
        assertThat(dto.ingredientCount()).isZero();
        verify(recipeRepository).findAllWithTagsAndIngredients();
    }

    @Test
    void test_shouldReturnFilteredRecipesWhenSearchProvided() {
        CategoryResponseDto categoryResponse = aCategoryResponse();
        when(recipeRepository.findAllWithTagsAndIngredientsBySearch("blyn"))
                .thenReturn(List.of(recipe));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        List<RecipeListResponseDto> result = recipeService.getAllWithIngredients("  blyn  ");

        assertThat(result).hasSize(1);
        verify(recipeRepository).findAllWithTagsAndIngredientsBySearch("blyn");
    }

    // ---------- getById ----------

    @Test
    void test_shouldReturnRecipeWhenExists() {
        RecipeResponseDto expected = aRecipeResponse();

        when(recipeRepository.findById(RECIPE_ID)).thenReturn(Optional.of(recipe));
        when(recipeMapper.toResponse(recipe)).thenReturn(expected);

        RecipeResponseDto result = recipeService.getById(RECIPE_ID);

        assertThat(result).isEqualTo(expected);
        verify(recipeRepository).findById(RECIPE_ID);
    }

    @Test
    void test_shouldThrowExceptionWhenRecipeNotFound() {
        when(recipeRepository.findById(RECIPE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.getById(RECIPE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    // ---------- create ----------

    @Test
    void test_shouldCreateRecipeWhenAllFieldsValid() {
        RecipeRequestDto request = validRequest();
        RecipeResponseDto expected = aRecipeResponse();

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(tagRepository.findByIdIn(Set.of(TAG_ID))).thenReturn(Set.of(tag));
        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.of(ingredient));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));
        when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(expected);

        RecipeResponseDto result = recipeService.create(request);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(captor.capture());
        Recipe saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo(RECIPE_NAME);
        assertThat(saved.getCategory()).isEqualTo(category);
        assertThat(saved.getDefaultServing()).isEqualTo((short) 4);
        assertThat(saved.getCookingTimeMinutes()).isEqualTo((short) 30);
        assertThat(saved.getIsFavorite()).isFalse();
        assertThat(saved.getNotes()).isEqualTo("notes");
        assertThat(saved.getTags()).containsExactly(tag);
        assertThat(saved.getLeftoverRecipe()).isNull();
        assertThat(saved.getIngredients()).hasSize(1);
        assertThat(saved.getIngredients().get(0).getIngredient()).isEqualTo(ingredient);
        assertThat(saved.getIngredients().get(0).getQuantity()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(result).isEqualTo(expected);
        verify(recipeRepository).flush();
    }

    @Test
    void test_shouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.create(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void test_shouldDefaultIsFavoriteToFalseWhenNullOnCreate() {
        RecipeRequestDto request = new RecipeRequestDto(
                RECIPE_NAME, CATEGORY_ID, (short) 4, (short) 30,
                Set.of(TAG_ID), null, null, "notes",
                List.of(new RecipeIngredientRequestDto(INGREDIENT_ID, new BigDecimal("100")))
        );

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(tagRepository.findByIdIn(Set.of(TAG_ID))).thenReturn(Set.of(tag));
        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.of(ingredient));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));
        when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(aRecipeResponse());

        recipeService.create(request);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(captor.capture());
        assertThat(captor.getValue().getIsFavorite()).isFalse();
    }

    @Test
    void test_shouldClearTagsWhenTagIdsIsNullOrEmpty() {
        RecipeRequestDto request = new RecipeRequestDto(
                RECIPE_NAME, CATEGORY_ID, (short) 4, (short) 30,
                null, null, false, "notes",
                List.of(new RecipeIngredientRequestDto(INGREDIENT_ID, new BigDecimal("100")))
        );

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.of(ingredient));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));
        when(recipeMapper.toResponse(any(Recipe.class))).thenReturn(aRecipeResponse());

        recipeService.create(request);

        ArgumentCaptor<Recipe> captor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(captor.capture());
        assertThat(captor.getValue().getTags()).isEmpty();
        verify(tagRepository, never()).findByIdIn(any());
    }

    @Test
    void test_shouldThrowExceptionWhenSomeTagsNotFound() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(tagRepository.findByIdIn(Set.of(TAG_ID))).thenReturn(Set.of());

        assertThatThrownBy(() -> recipeService.create(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("tags not found");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void test_shouldThrowExceptionWhenLeftoverRecipeNotFound() {
        RecipeRequestDto request = new RecipeRequestDto(
                RECIPE_NAME, CATEGORY_ID, (short) 4, (short) 30,
                Set.of(TAG_ID), LEFTOVER_RECIPE_ID, false, "notes",
                List.of(new RecipeIngredientRequestDto(INGREDIENT_ID, new BigDecimal("100")))
        );

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(tagRepository.findByIdIn(Set.of(TAG_ID))).thenReturn(Set.of(tag));
        when(recipeRepository.findById(LEFTOVER_RECIPE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Leftover recipe not found");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void test_shouldThrowExceptionWhenAnyIngredientNotFound() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(tagRepository.findByIdIn(Set.of(TAG_ID))).thenReturn(Set.of(tag));
        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.create(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ingredient not found");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    // ---------- update ----------

    @Test
    void test_shouldUpdateRecipeWhenAllFieldsValid() {
        String newName = "Blynai su uogiene";
        RecipeRequestDto request = new RecipeRequestDto(
                newName, CATEGORY_ID, (short) 6, (short) 45,
                Set.of(TAG_ID), null, true, "new notes",
                List.of(new RecipeIngredientRequestDto(INGREDIENT_ID, new BigDecimal("200")))
        );
        RecipeResponseDto expected = aRecipeResponse();

        when(recipeRepository.findById(RECIPE_ID)).thenReturn(Optional.of(recipe));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(tagRepository.findByIdIn(Set.of(TAG_ID))).thenReturn(Set.of(tag));
        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.of(ingredient));
        when(recipeRepository.save(recipe)).thenReturn(recipe);
        when(recipeMapper.toResponse(recipe)).thenReturn(expected);

        RecipeResponseDto result = recipeService.update(RECIPE_ID, request);

        assertThat(recipe.getName()).isEqualTo(newName);
        assertThat(recipe.getDefaultServing()).isEqualTo((short) 6);
        assertThat(recipe.getCookingTimeMinutes()).isEqualTo((short) 45);
        assertThat(recipe.getIsFavorite()).isTrue();
        assertThat(recipe.getNotes()).isEqualTo("new notes");
        assertThat(recipe.getTags()).containsExactly(tag);
        assertThat(recipe.getLeftoverRecipe()).isNull();
        assertThat(recipe.getIngredients()).hasSize(1);
        assertThat(result).isEqualTo(expected);
        verify(recipeRepository).save(recipe);
    }

    @Test
    void test_shouldThrowExceptionWhenRecipeNotFoundOnUpdate() {
        when(recipeRepository.findById(RECIPE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.update(RECIPE_ID, validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recipe not found");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    void test_shouldThrowExceptionWhenCategoryNotFoundOnUpdate() {
        when(recipeRepository.findById(RECIPE_ID)).thenReturn(Optional.of(recipe));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recipeService.update(RECIPE_ID, validRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");

        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    // ---------- delete ----------

    @Test
    void test_shouldDeleteRecipeWhenExists() {
        when(recipeRepository.existsById(RECIPE_ID)).thenReturn(true);

        recipeService.delete(RECIPE_ID);

        verify(recipeRepository).deleteById(RECIPE_ID);
    }

    @Test
    void test_shouldThrowExceptionWhenDeletingNonExistentRecipe() {
        when(recipeRepository.existsById(RECIPE_ID)).thenReturn(false);

        assertThatThrownBy(() -> recipeService.delete(RECIPE_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(recipeRepository, never()).deleteById(RECIPE_ID);
    }
}
