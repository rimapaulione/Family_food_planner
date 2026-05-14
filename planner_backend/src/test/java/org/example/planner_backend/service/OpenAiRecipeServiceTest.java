package org.example.planner_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.planner_backend.dto.ai.GenerateRecipeRequestDto;
import org.example.planner_backend.dto.ai.GenerateRecipeResponseDto;
import org.example.planner_backend.exception.BadRequestException;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.model.enums.Unit;
import org.example.planner_backend.repository.CategoryRepository;
import org.example.planner_backend.repository.IngredientRepository;
import org.example.planner_backend.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiRecipeServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final UUID FAMILY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID INGREDIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String MODEL = "gpt-test";

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private IngredientMatcher ingredientMatcher;

    @Mock
    private FamilyResolver familyResolver;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RestClient openAiRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec uriSpec;

    @Mock(answer = Answers.RETURNS_SELF)
    private RestClient.RequestBodySpec bodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private OpenAiRecipeService service;

    private Family family;
    private Ingredient familyIngredient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "model", MODEL);

        family = Family.builder().id(FAMILY_ID).build();
        familyIngredient = Ingredient.builder()
                .id(INGREDIENT_ID)
                .nameLt("Pomidoras")
                .unit(Unit.VNT)
                .build();

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(categoryRepository.findAll()).thenReturn(List.of());
        when(tagRepository.findAll()).thenReturn(List.of());

        when(openAiRestClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("/chat/completions")).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void generate_shouldReturnMatchedAndMissingIngredients() {
        when(ingredientRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of(familyIngredient));

        stubOpenAiContent("""
                {
                  "name": "Pomidorų sriuba",
                  "categoryId": 1,
                  "defaultServing": 4,
                  "cookingTimeMinutes": 30,
                  "notes": "Virkite 30 min.",
                  "tagIds": [5, 7],
                  "ingredients": [
                    {"name": "Pomidoras", "quantity": 3, "unit": "VNT"},
                    {"name": "Druska",    "quantity": 5, "unit": "G"}
                  ]
                }
                """);

        when(ingredientMatcher.matchIngredients("Pomidoras", List.of(familyIngredient)))
                .thenReturn(Optional.of(familyIngredient));
        when(ingredientMatcher.matchIngredients("Druska", List.of(familyIngredient)))
                .thenReturn(Optional.empty());

        GenerateRecipeResponseDto response = service.generate(EMAIL, new GenerateRecipeRequestDto("padaryk sriubą"));

        assertThat(response.name()).isEqualTo("Pomidorų sriuba");
        assertThat(response.categoryId()).isEqualTo(1);
        assertThat(response.defaultServing()).isEqualTo(4);
        assertThat(response.cookingTimeMinutes()).isEqualTo(30);
        assertThat(response.notes()).isEqualTo("Virkite 30 min.");
        assertThat(response.tagIds()).containsExactly(5, 7);

        assertThat(response.ingredients()).hasSize(1);
        assertThat(response.ingredients().get(0).ingredientId()).isEqualTo(INGREDIENT_ID);
        assertThat(response.ingredients().get(0).quantity()).isEqualByComparingTo("3");
        assertThat(response.ingredients().get(0).unit()).isEqualTo("VNT");

        assertThat(response.missingIngredients()).hasSize(1);
        assertThat(response.missingIngredients().get(0).name()).isEqualTo("Druska");
        assertThat(response.missingIngredients().get(0).quantity()).isEqualByComparingTo("5");
        assertThat(response.missingIngredients().get(0).unit()).isEqualTo("G");
    }

    @Test
    void generate_shouldSumQuantitiesWhenSameIngredientMatchedTwice() {
        when(ingredientRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of(familyIngredient));

        stubOpenAiContent("""
                {
                  "name": "Salotos",
                  "categoryId": 2,
                  "defaultServing": 2,
                  "cookingTimeMinutes": 10,
                  "notes": "Sumaišykite.",
                  "tagIds": [],
                  "ingredients": [
                    {"name": "Pomidoras",   "quantity": 2, "unit": "VNT"},
                    {"name": "pomidoriukas","quantity": 1, "unit": "G"}
                  ]
                }
                """);

        when(ingredientMatcher.matchIngredients("Pomidoras", List.of(familyIngredient)))
                .thenReturn(Optional.of(familyIngredient));
        when(ingredientMatcher.matchIngredients("pomidoriukas", List.of(familyIngredient)))
                .thenReturn(Optional.of(familyIngredient));

        GenerateRecipeResponseDto response = service.generate(EMAIL, new GenerateRecipeRequestDto("salotos"));

        assertThat(response.ingredients()).hasSize(1);
        assertThat(response.ingredients().get(0).ingredientId()).isEqualTo(INGREDIENT_ID);
        assertThat(response.ingredients().get(0).quantity()).isEqualByComparingTo("3");
        assertThat(response.ingredients().get(0).unit()).isEqualTo("VNT");
        assertThat(response.missingIngredients()).isEmpty();
    }

    @Test
    void generate_shouldThrowBadRequestWhenOpenAiCallThrows() {
        when(ingredientRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of());
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> service.generate(EMAIL, new GenerateRecipeRequestDto("anything")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("OpenAI request failed");
    }

    @Test
    void generate_shouldThrowBadRequestWhenOpenAiReturnsNull() {
        when(ingredientRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of());
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(null);

        assertThatThrownBy(() -> service.generate(EMAIL, new GenerateRecipeRequestDto("anything")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty response");
    }

    @Test
    void generate_shouldThrowBadRequestWhenContentIsMalformedJson() {
        when(ingredientRepository.findByFamilyId(FAMILY_ID)).thenReturn(List.of());
        stubOpenAiContent("not-json-at-all");

        assertThatThrownBy(() -> service.generate(EMAIL, new GenerateRecipeRequestDto("anything")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Could not parse");
    }

    private void stubOpenAiContent(String content) {
        Map<String, Object> response = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("content", content))
                )
        );
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);
    }
}
