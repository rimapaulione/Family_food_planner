package org.example.planner_backend.service;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.example.planner_backend.model.entity.Ingredient;
import org.example.planner_backend.util.TextUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class IngredientMatcher {

    private static final int MAX_EDIT_DISTANCE = 2;
    private static final LevenshteinDistance LEVENSHTEIN = LevenshteinDistance.getDefaultInstance();

    public Optional<Ingredient> matchIngredients(final String name, final List<Ingredient> familyIngredients) {
        for (Ingredient i : familyIngredients) {
            if (i.getNameLt().equalsIgnoreCase(name)) return Optional.of(i);
        }

        String target = TextUtil.normalize(name);
        for (Ingredient i : familyIngredients) {
            if (TextUtil.normalize(i.getNameLt()).equals(target)) return Optional.of(i);
        }

        Ingredient closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (Ingredient i : familyIngredients) {
            int distance = LEVENSHTEIN.apply(target, TextUtil.normalize(i.getNameLt()));
            if (distance <= MAX_EDIT_DISTANCE && distance < closestDistance) {
                closest = i;
                closestDistance = distance;
            }
        }
        return Optional.ofNullable(closest);
    }
}
