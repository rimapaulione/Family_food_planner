package org.example.planner_backend.service;

import org.example.planner_backend.dto.ingredient.IngredientCheckNameResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto;
import org.example.planner_backend.dto.ingredient.IngredientRequestDto;
import org.example.planner_backend.dto.ingredient.IngredientResponseDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.mapper.IngredientMapper;
import org.example.planner_backend.model.entity.Family;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    private static final UUID INGREDIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FAMILY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String EMAIL = "user@example.com";
    private static final String INGREDIENT_NAME = "Pienas";
    private static final String CHANGED_INGREDIENT_NAME = "Pienelis";

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private FamilyResolver familyResolver;

    @Mock
    private IngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientService ingredientService;

    private Family family;
    private Ingredient milk;
    private IngredientResponseDto milkResponse;

    @BeforeEach
    void setUp() {
        family = Family.builder().id(FAMILY_ID).name("F").build();
        milk = Ingredient.builder()
                .id(INGREDIENT_ID)
                .nameLt(INGREDIENT_NAME)
                .unit(Unit.ML)
                .family(family)
                .build();
        milkResponse = new IngredientResponseDto(INGREDIENT_ID, INGREDIENT_NAME, Unit.ML);
    }

    private void mockFamilyId() {
        when(familyResolver.getFamilyIdByEmail(EMAIL)).thenReturn(FAMILY_ID);
    }

    private void mockFamily() {
        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
    }

    // ---------- getAll ----------

    @Test
    void test_shouldReturnAllIngredientsWhenSearchIsBlank() {
        mockFamilyId();
        when(ingredientRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of(milk));
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        List<IngredientResponseDto> result = ingredientService.getAll(EMAIL, "  ");

        assertThat(result).containsExactly(milkResponse);
        verify(ingredientRepository).findByFamilyId(FAMILY_ID);
    }

    @Test
    void test_shouldReturnFilteredIngredientsWhenSearchProvided() {
        mockFamilyId();
        when(ingredientRepository.findByFamilyIdAndNameLtContainingIgnoreCase(FAMILY_ID, "milk"))
                .thenReturn(List.of(milk));
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        List<IngredientResponseDto> result = ingredientService.getAll(EMAIL, "  milk  ");

        assertThat(result).containsExactly(milkResponse);
        verify(ingredientRepository).findByFamilyIdAndNameLtContainingIgnoreCase(FAMILY_ID, "milk");
    }

    // ---------- getAllWithRecipesCount ----------

    @Test
    void test_shouldReturnAllDetailsWhenSearchIsBlank() {
        mockFamilyId();
        IngredientDetailResponseDto detail = new IngredientDetailResponseDto(
                INGREDIENT_ID, INGREDIENT_NAME, Unit.ML, 2);
        when(ingredientRepository.findAllWithRecipeCount(FAMILY_ID, null)).thenReturn(List.of(detail));

        List<IngredientDetailResponseDto> result = ingredientService.getAllWithRecipesCount(EMAIL, "  ");

        assertThat(result).containsExactly(detail);
        verify(ingredientRepository).findAllWithRecipeCount(FAMILY_ID, null);
    }

    @Test
    void test_shouldReturnDetailsUsingTrimmedSearch() {
        mockFamilyId();
        IngredientDetailResponseDto detail = new IngredientDetailResponseDto(
                INGREDIENT_ID, INGREDIENT_NAME, Unit.ML, 2);
        when(ingredientRepository.findAllWithRecipeCount(FAMILY_ID, "milk")).thenReturn(List.of(detail));

        List<IngredientDetailResponseDto> result = ingredientService.getAllWithRecipesCount(EMAIL, "  milk  ");

        assertThat(result).containsExactly(detail);
        verify(ingredientRepository).findAllWithRecipeCount(FAMILY_ID, "milk");
    }

    // ---------- checkName ----------

    @Test
    void test_shouldReturnEmptyWhenNameTooShort() {
        mockFamilyId();
        IngredientCheckNameResponseDto result = ingredientService.checkName(EMAIL, "a");

        assertThat(result.exactMatch()).isFalse();
        assertThat(result.existingName()).isNull();
        assertThat(result.similarNames()).isEmpty();
        verify(ingredientRepository, never()).findByFamilyIdAndNameLtIgnoreCaseStartingWith(any(), anyString());
    }

    @Test
    void test_shouldReturnExactMatchWhenNameExistsIgnoringCase() {
        mockFamilyId();
        when(ingredientRepository.findByFamilyIdAndNameLtIgnoreCaseStartingWith(FAMILY_ID, "pien"))
                .thenReturn(List.of(milk));

        IngredientCheckNameResponseDto result = ingredientService.checkName(EMAIL, "pienas");

        assertThat(result.exactMatch()).isTrue();
        assertThat(result.existingName()).isEqualTo(INGREDIENT_NAME);
        assertThat(result.similarNames()).isEmpty();
    }

    @Test
    void test_shouldReturnSimilarNamesWhenNoExactMatch() {
        mockFamilyId();
        Ingredient pienelis = Ingredient.builder().nameLt("Pienelis").unit(Unit.ML).family(family).build();
        Ingredient pienukas = Ingredient.builder().nameLt("Pienukas").unit(Unit.ML).family(family).build();
        when(ingredientRepository.findByFamilyIdAndNameLtIgnoreCaseStartingWith(FAMILY_ID, "Pien"))
                .thenReturn(List.of(pienelis, pienukas));

        IngredientCheckNameResponseDto result = ingredientService.checkName(EMAIL, "Piena");

        assertThat(result.exactMatch()).isFalse();
        assertThat(result.existingName()).isNull();
        assertThat(result.similarNames()).containsExactly("Pienelis", "Pienukas");
    }

    @Test
    void test_shouldUseFullNameAsPrefixWhenNameShorterThanFour() {
        mockFamilyId();
        when(ingredientRepository.findByFamilyIdAndNameLtIgnoreCaseStartingWith(FAMILY_ID, "Pi"))
                .thenReturn(List.of());

        ingredientService.checkName(EMAIL, "Pi");

        verify(ingredientRepository).findByFamilyIdAndNameLtIgnoreCaseStartingWith(FAMILY_ID, "Pi");
    }

    // ---------- create ----------

    @Test
    void test_shouldCreateIngredientWhenNameIsUnique() {
        mockFamily();
        IngredientRequestDto newIngredient = new IngredientRequestDto(INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.existsByFamilyIdAndNameLtIgnoreCase(FAMILY_ID, INGREDIENT_NAME)).thenReturn(false);
        when(ingredientRepository.save(any(Ingredient.class))).thenReturn(milk);
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        IngredientResponseDto result = ingredientService.create(EMAIL, newIngredient);

        ArgumentCaptor<Ingredient> captor = ArgumentCaptor.forClass(Ingredient.class);
        verify(ingredientRepository).save(captor.capture());
        Ingredient saved = captor.getValue();

        assertThat(saved.getNameLt()).isEqualTo(INGREDIENT_NAME);
        assertThat(saved.getUnit()).isEqualTo(Unit.ML);
        assertThat(saved.getFamily()).isEqualTo(family);
        assertThat(result).isEqualTo(milkResponse);
    }

    @Test
    void test_shouldThrowExceptionWhenCreatingWithExistingName() {
        mockFamily();
        IngredientRequestDto newIngredient = new IngredientRequestDto(INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.existsByFamilyIdAndNameLtIgnoreCase(FAMILY_ID, INGREDIENT_NAME)).thenReturn(true);

        assertThatThrownBy(() -> ingredientService.create(EMAIL, newIngredient))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(ingredientRepository, never()).save(any(Ingredient.class));
    }

    // ---------- update ----------

    @Test
    void test_shouldUpdateIngredientWhenIngredientExistsAndIsUnique() {
        mockFamilyId();
        IngredientRequestDto updateIngredient = new IngredientRequestDto(CHANGED_INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.findByIdAndFamilyId(INGREDIENT_ID, FAMILY_ID)).thenReturn(Optional.of(milk));
        when(ingredientRepository.existsByFamilyIdAndNameLtIgnoreCase(FAMILY_ID, CHANGED_INGREDIENT_NAME)).thenReturn(false);
        when(ingredientRepository.save(milk)).thenReturn(milk);
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        IngredientResponseDto result = ingredientService.update(EMAIL, INGREDIENT_ID, updateIngredient);

        assertThat(milk.getNameLt()).isEqualTo(CHANGED_INGREDIENT_NAME);
        assertThat(milk.getUnit()).isEqualTo(Unit.ML);
        assertThat(result).isEqualTo(milkResponse);
    }

    @Test
    void test_shouldUpdateIngredientWhenIngredientNameIsUnchanged() {
        mockFamilyId();
        IngredientRequestDto updateIngredient = new IngredientRequestDto(INGREDIENT_NAME, Unit.G);

        when(ingredientRepository.findByIdAndFamilyId(INGREDIENT_ID, FAMILY_ID)).thenReturn(Optional.of(milk));
        when(ingredientRepository.save(milk)).thenReturn(milk);
        when(ingredientMapper.toResponse(milk)).thenReturn(milkResponse);

        IngredientResponseDto result = ingredientService.update(EMAIL, INGREDIENT_ID, updateIngredient);

        assertThat(milk.getNameLt()).isEqualTo(INGREDIENT_NAME);
        assertThat(milk.getUnit()).isEqualTo(Unit.G);
        assertThat(result).isEqualTo(milkResponse);
        verify(ingredientRepository, never()).existsByFamilyIdAndNameLtIgnoreCase(eq(FAMILY_ID), anyString());
    }

    @Test
    void test_shouldThrowExceptionWhenUpdatingNonExistentIngredient() {
        mockFamilyId();
        IngredientRequestDto updateIngredient = new IngredientRequestDto(CHANGED_INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.findByIdAndFamilyId(INGREDIENT_ID, FAMILY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.update(EMAIL, INGREDIENT_ID, updateIngredient))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(ingredientRepository, never()).save(any(Ingredient.class));
    }

    @Test
    void test_shouldThrowExceptionWhenUpdatingToExistingName() {
        mockFamilyId();
        IngredientRequestDto updateIngredient = new IngredientRequestDto(CHANGED_INGREDIENT_NAME, Unit.ML);

        when(ingredientRepository.findByIdAndFamilyId(INGREDIENT_ID, FAMILY_ID)).thenReturn(Optional.of(milk));
        when(ingredientRepository.existsByFamilyIdAndNameLtIgnoreCase(FAMILY_ID, CHANGED_INGREDIENT_NAME)).thenReturn(true);

        assertThatThrownBy(() -> ingredientService.update(EMAIL, INGREDIENT_ID, updateIngredient))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(ingredientRepository, never()).save(any(Ingredient.class));
    }

    // ---------- delete ----------

    @Test
    void test_shouldDeleteIngredientWhenIngredientExistsAndNotInRecipe() {
        mockFamilyId();
        when(ingredientRepository.findByIdAndFamilyId(INGREDIENT_ID, FAMILY_ID)).thenReturn(Optional.of(milk));
        when(ingredientRepository.countRecipesByIngredientId(INGREDIENT_ID)).thenReturn(0);

        ingredientService.delete(EMAIL, INGREDIENT_ID);

        verify(ingredientRepository).delete(milk);
    }

    @Test
    void test_shouldThrowExceptionWhenDeletingNonExistentIngredient() {
        mockFamilyId();
        when(ingredientRepository.findByIdAndFamilyId(INGREDIENT_ID, FAMILY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.delete(EMAIL, INGREDIENT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(ingredientRepository, never()).delete(any(Ingredient.class));
    }

    @Test
    void test_shouldThrowExceptionWhenDeletingIngredientUsedInRecipe() {
        mockFamilyId();
        int numberOfRecipe = 1;

        when(ingredientRepository.findByIdAndFamilyId(INGREDIENT_ID, FAMILY_ID)).thenReturn(Optional.of(milk));
        when(ingredientRepository.countRecipesByIngredientId(INGREDIENT_ID)).thenReturn(numberOfRecipe);

        assertThatThrownBy(() -> ingredientService.delete(EMAIL, INGREDIENT_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cannot be deleted");

        verify(ingredientRepository, never()).delete(any(Ingredient.class));
    }
}
