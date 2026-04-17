package org.example.planner_backend.service;

import org.example.planner_backend.dto.ingredient.IngredientCheckNameResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientRequestDto;
import org.example.planner_backend.dto.ingredient.IngredientResponseDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.mapper.IngredientMapper;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.model.enums.Unit;
import org.example.planner_backend.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    private static final UUID INGREDIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String INGREDIENT_NAME = "Pienas";
    private static final String CHANGED_INGREDIENT_NAME = "Pienelis";

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientService ingredientService;

    private Ingredient milk;
    private IngredientResponseDto milkResponse;

    @BeforeEach
    void setUp() {
        milk = Ingredient.builder()
                .id(INGREDIENT_ID)
                .nameLt(INGREDIENT_NAME)
                .unit(Unit.ML)
                .build();
        milkResponse = new IngredientResponseDto(INGREDIENT_ID, INGREDIENT_NAME, Unit.ML);
    }

    // ---------- getAll ----------

    @Test
    void test_shouldReturnAllIngredientsWhenSearchIsBlank() {
        when(ingredientRepository.findAll()).thenReturn(List.of(milk));
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        List<IngredientResponseDto> result = ingredientService.getAll("  ");

        assertThat(result).containsExactly(milkResponse);
        verify(ingredientRepository).findAll();
        verify(ingredientRepository, never()).findByNameLtContainingIgnoreCase(anyString());
    }

    @Test
    void test_shouldReturnFilteredIngredientsWhenSearchProvided() {
        when(ingredientRepository.findByNameLtContainingIgnoreCase("milk"))
                .thenReturn(List.of(milk));
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        List<IngredientResponseDto> result = ingredientService.getAll("  milk  ");

        assertThat(result).containsExactly(milkResponse);
        verify(ingredientRepository).findByNameLtContainingIgnoreCase("milk");
        verify(ingredientRepository, never()).findAll();
    }

    // ---------- getAllWithRecipesCount ----------

    @Test
    void test_shouldReturnAllDetailsWhenSearchIsBlank() {
        IngredientDetailResponseDto detail = new IngredientDetailResponseDto(
                INGREDIENT_ID, INGREDIENT_NAME, Unit.ML, 2);
        when(ingredientRepository.findAllWithRecipeCount(null)).thenReturn(List.of(detail));

        List<IngredientDetailResponseDto> result = ingredientService.getAllWithRecipesCount("  ");

        assertThat(result).containsExactly(detail);
        verify(ingredientRepository).findAllWithRecipeCount(null);
    }

    @Test
    void test_shouldReturnDetailsUsingTrimmedSearch() {
        IngredientDetailResponseDto detail = new IngredientDetailResponseDto(
                INGREDIENT_ID, INGREDIENT_NAME, Unit.ML, 2);
        when(ingredientRepository.findAllWithRecipeCount("milk")).thenReturn(List.of(detail));

        List<IngredientDetailResponseDto> result = ingredientService.getAllWithRecipesCount("  milk  ");

        assertThat(result).containsExactly(detail);
        verify(ingredientRepository).findAllWithRecipeCount("milk");
    }

    // ---------- checkName ----------

    @Test
    void test_shouldReturnEmptyWhenNameTooShort() {
        IngredientCheckNameResponseDto result = ingredientService.checkName("a");

        assertThat(result.exactMatch()).isFalse();
        assertThat(result.existingName()).isNull();
        assertThat(result.similarNames()).isEmpty();
        verify(ingredientRepository, never()).findByNameLtIgnoreCaseStartingWith(anyString());
    }

    @Test
    void test_shouldReturnExactMatchWhenNameExistsIgnoringCase() {
        when(ingredientRepository.findByNameLtIgnoreCaseStartingWith("pien"))
                .thenReturn(List.of(milk));

        IngredientCheckNameResponseDto result = ingredientService.checkName("pienas");

        assertThat(result.exactMatch()).isTrue();
        assertThat(result.existingName()).isEqualTo(INGREDIENT_NAME);
        assertThat(result.similarNames()).isEmpty();
    }

    @Test
    void test_shouldReturnSimilarNamesWhenNoExactMatch() {
        Ingredient pienelis = Ingredient.builder().nameLt("Pienelis").unit(Unit.ML).build();
        Ingredient pienukas = Ingredient.builder().nameLt("Pienukas").unit(Unit.ML).build();
        when(ingredientRepository.findByNameLtIgnoreCaseStartingWith("Pien"))
                .thenReturn(List.of(pienelis, pienukas));

        IngredientCheckNameResponseDto result = ingredientService.checkName("Piena");

        assertThat(result.exactMatch()).isFalse();
        assertThat(result.existingName()).isNull();
        assertThat(result.similarNames()).containsExactly("Pienelis", "Pienukas");
    }

    @Test
    void test_shouldUseFullNameAsPrefixWhenNameShorterThanFour() {
        when(ingredientRepository.findByNameLtIgnoreCaseStartingWith("Pi"))
                .thenReturn(List.of());

        ingredientService.checkName("Pi");

        verify(ingredientRepository).findByNameLtIgnoreCaseStartingWith("Pi");
    }

    // ---------- create ----------

    @Test
    void test_shouldCreateIngredientWhenNameIsUnique() {
        IngredientRequestDto newIngredient = new IngredientRequestDto(INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.existsByNameLtIgnoreCase(INGREDIENT_NAME)).thenReturn(false);
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(milk);
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        IngredientResponseDto result = ingredientService.create(newIngredient);

        ArgumentCaptor<Ingredient> captor = ArgumentCaptor.forClass(Ingredient.class);
        verify(ingredientRepository).save(captor.capture());
        Ingredient saved = captor.getValue();

        assertThat(saved.getNameLt()).isEqualTo(INGREDIENT_NAME);
        assertThat(saved.getUnit()).isEqualTo(Unit.ML);
        assertThat(saved.getId()).isNull();
        assertThat(result).isEqualTo(milkResponse);
    }

    @Test
    void test_shouldThrowExceptionWhenCreatingWithExistingName() {
        IngredientRequestDto newIngredient = new IngredientRequestDto(INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.existsByNameLtIgnoreCase(INGREDIENT_NAME)).thenReturn(true);

        assertThatThrownBy(() -> ingredientService.create(newIngredient))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(ingredientRepository, never()).save(any(Ingredient.class));
    }

    // ---------- update ----------

    @Test
    void test_shouldUpdateIngredientWhenIngredientExistsAndIsUnique() {
        IngredientRequestDto updateIngredient = new IngredientRequestDto(CHANGED_INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.of(milk));
        when(ingredientRepository.existsByNameLtIgnoreCase(CHANGED_INGREDIENT_NAME)).thenReturn(false);
        when(ingredientRepository.save(milk)).thenReturn(milk);
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        IngredientResponseDto result = ingredientService.update(INGREDIENT_ID, updateIngredient);

        assertThat(milk.getNameLt()).isEqualTo(CHANGED_INGREDIENT_NAME);
        assertThat(milk.getUnit()).isEqualTo(Unit.ML);
        assertThat(result).isEqualTo(milkResponse);
        verify(ingredientRepository).save(milk);
    }

    @Test
    void test_shouldUpdateIngredientWhenIngredientNameIsUnchanged() {
        IngredientRequestDto updateIngredient = new IngredientRequestDto(INGREDIENT_NAME, Unit.G);

        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.of(milk));
        when(ingredientRepository.save(milk)).thenReturn(milk);
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        IngredientResponseDto result = ingredientService.update(INGREDIENT_ID, updateIngredient);

        assertThat(milk.getNameLt()).isEqualTo(INGREDIENT_NAME);
        assertThat(milk.getUnit()).isEqualTo(Unit.G);
        assertThat(result).isEqualTo(milkResponse);
        verify(ingredientRepository).save(milk);
        verify(ingredientRepository, never()).existsByNameLtIgnoreCase(anyString());
    }

    @Test
    void test_shouldThrowExceptionWhenUpdatingNonExistentIngredient() {
        IngredientRequestDto updateIngredient = new IngredientRequestDto(CHANGED_INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.update(INGREDIENT_ID, updateIngredient))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(ingredientRepository, never()).save(any(Ingredient.class));
    }

    @Test
    void test_shouldThrowExceptionWhenUpdatingToExistingName() {
        IngredientRequestDto updateIngredient = new IngredientRequestDto(CHANGED_INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.findById(INGREDIENT_ID)).thenReturn(Optional.of(milk));
        when(ingredientRepository.existsByNameLtIgnoreCase(CHANGED_INGREDIENT_NAME)).thenReturn(true);

        assertThatThrownBy(() -> ingredientService.update(INGREDIENT_ID, updateIngredient))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(ingredientRepository, never()).save(any(Ingredient.class));
    }

    // ---------- delete ----------

    @Test
    void test_shouldDeleteIngredientWhenIngredientExistsAndNotInRecipe() {
        when(ingredientRepository.existsById(INGREDIENT_ID)).thenReturn(true);
        when(ingredientRepository.countRecipesByIngredientId(INGREDIENT_ID)).thenReturn(0);

        ingredientService.delete(INGREDIENT_ID);

        verify(ingredientRepository).deleteById(INGREDIENT_ID);
    }

    @Test
    void test_shouldThrowExceptionWhenDeletingNonExistentIngredient() {
        when(ingredientRepository.existsById(INGREDIENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> ingredientService.delete(INGREDIENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(ingredientRepository, never()).deleteById(INGREDIENT_ID);
    }

    @Test
    void test_shouldThrowExceptionWhenDeletingIngredientUsedInRecipe() {
        int numberOfRecipe = 1;

        when(ingredientRepository.existsById(INGREDIENT_ID)).thenReturn(true);
        when(ingredientRepository.countRecipesByIngredientId(INGREDIENT_ID)).thenReturn(numberOfRecipe);

        assertThatThrownBy(() -> ingredientService.delete(INGREDIENT_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cannot be deleted");

        verify(ingredientRepository, never()).deleteById(INGREDIENT_ID);
    }
}