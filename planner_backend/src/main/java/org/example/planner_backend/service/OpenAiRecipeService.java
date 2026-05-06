package org.example.planner_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.ai.GenerateRecipeRequestDto;
import org.example.planner_backend.dto.ai.GenerateRecipeResponseDto;
import org.example.planner_backend.dto.ai.MatchedIngredientDto;
import org.example.planner_backend.dto.ai.MissingIngredientDto;
import org.example.planner_backend.exception.BadRequestException;
import org.example.planner_backend.model.entity.Category;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.model.entity.Tag;
import org.example.planner_backend.repository.CategoryRepository;
import org.example.planner_backend.repository.IngredientRepository;
import org.example.planner_backend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OpenAiRecipeService {

    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final IngredientMatcher ingredientMatcher;
    private final FamilyResolver familyResolver;
    private final ObjectMapper objectMapper;
    private final RestClient openAiRestClient;

    @Value("${openai.model}")
    private String model;

    public GenerateRecipeResponseDto generate(final String email, final GenerateRecipeRequestDto request) {
        Family family = familyResolver.getFamilyByEmail(email);
        List<Ingredient> familyIngredients = ingredientRepository.findByFamilyId(family.getId());
        List<Category> categories = categoryRepository.findAll();
        List<Tag> tags = tagRepository.findAll();

        String systemPrompt = this.buildSystemPrompt(familyIngredients, categories, tags);
        AiRecipe aiRecipe = this.callOpenAi(systemPrompt, request.prompt());

        Map<UUID, BigDecimal> matchedQuantityById = new LinkedHashMap<>();
        Map<UUID, String> matchedUnitById = new HashMap<>();
        List<MissingIngredientDto> missing = new ArrayList<>();

        for (AiIngredient aiIngredient : aiRecipe.ingredients()) {
            Optional<Ingredient> matchedIngredient = ingredientMatcher
                    .matchIngredients(aiIngredient.name(), familyIngredients);
            if (matchedIngredient.isPresent()) {
                UUID id = matchedIngredient.get().getId();
                matchedQuantityById.merge(id, aiIngredient.quantity(), BigDecimal::add);
                matchedUnitById.putIfAbsent(id, aiIngredient.unit());
            } else {
                missing.add(new MissingIngredientDto(
                        aiIngredient.name(), aiIngredient.quantity(), aiIngredient.unit()));
            }
        }

        List<MatchedIngredientDto> matched = matchedQuantityById.entrySet().stream()
                .map(e -> new MatchedIngredientDto(e.getKey(), e.getValue(), matchedUnitById.get(e.getKey())))
                .toList();

        return new GenerateRecipeResponseDto(
                aiRecipe.name(),
                aiRecipe.categoryId(),
                aiRecipe.defaultServing(),
                aiRecipe.cookingTimeMinutes(),
                aiRecipe.notes(),
                aiRecipe.tagIds(),
                matched,
                missing
        );
    }

    private String buildSystemPrompt(
            final List<Ingredient> familyIngredients,
            final List<Category> categories,
            final List<Tag> tags
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("You generate a Lithuanian recipe based on the user's prompt. ");
        sb.append("Output JSON matching the provided schema. ");
        sb.append("Recipe name MUST be in Lithuanian.\n\n");

        sb.append("Available categories (use one categoryId):\n");
        for (Category c : categories) {
            sb.append("- ").append(c.getId()).append(": ").append(c.getName()).append("\n");
        }

        sb.append("\nAvailable tags (zero or more tagIds):\n");
        for (Tag t : tags) {
            sb.append("- ").append(t.getId()).append(": ").append(t.getName()).append("\n");
        }

        sb.append("\nAvailable ingredients in family's library (prefer these names exactly, ");
        sb.append("Lithuanian, with the listed unit):\n");
        for (Ingredient i : familyIngredients) {
            sb.append("- ").append(i.getNameLt()).append(" (").append(i.getUnit().name()).append(")\n");
        }

        sb.append("\nIf a recipe genuinely needs an ingredient not in the library, ");
        sb.append("use its Lithuanian name and an appropriate unit (VNT, G, or ML). ");
        sb.append("Quantities must match the requested serving count.\n\n");

        sb.append("Include short cooking instructions in 'notes' field (Lithuanian, ");
        sb.append("2-4 short sentences, max 500 characters, just the essential steps).");

        return sb.toString();
    }

    private AiRecipe callOpenAi(final String systemPrompt, final String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "response_format", this.buildResponseFormat()
        );

        Map<String, Object> response;
        try {
            response = openAiRestClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            throw new BadRequestException("OpenAI request failed: " + e.getMessage());
        }

        if (response == null) {
            throw new BadRequestException("OpenAI returned an empty response");
        }

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            return objectMapper.readValue(content, AiRecipe.class);
        } catch (Exception e) {
            throw new BadRequestException("Could not parse OpenAI response: " + e.getMessage());
        }
    }

    private Map<String, Object> buildResponseFormat() {
        Map<String, Object> ingredientItem = Map.of(
                "type", "object",
                "properties", Map.of(
                        "name", Map.of("type", "string"),
                        "quantity", Map.of("type", "number"),
                        "unit", Map.of("type", "string", "enum", List.of("VNT", "G", "ML"))
                ),
                "required", List.of("name", "quantity", "unit"),
                "additionalProperties", false
        );

        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", new LinkedHashMap<String, Object>() {{
                    put("name", Map.of("type", "string"));
                    put("categoryId", Map.of("type", "integer"));
                    put("defaultServing", Map.of("type", "integer"));
                    put("cookingTimeMinutes", Map.of("type", "integer"));
                    put("notes", Map.of("type", "string"));
                    put("tagIds", Map.of("type", "array", "items", Map.of("type", "integer")));
                    put("ingredients", Map.of("type", "array", "items", ingredientItem));
                }},
                "required", List.of("name", "categoryId", "defaultServing", "cookingTimeMinutes", "notes", "tagIds", "ingredients"),
                "additionalProperties", false
        );

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "recipe",
                        "strict", true,
                        "schema", schema
                )
        );
    }

    private record AiRecipe(
            String name,
            Integer categoryId,
            Integer defaultServing,
            Integer cookingTimeMinutes,
            String notes,
            List<Integer> tagIds,
            List<AiIngredient> ingredients
    ) {
    }

    private record AiIngredient(
            String name,
            BigDecimal quantity,
            String unit
    ) {
    }
}
