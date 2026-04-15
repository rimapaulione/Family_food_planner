package org.example.planner_backend.repository;

import org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto;
import org.example.planner_backend.model.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    List<Ingredient> findByNameLtContainingIgnoreCase(String nameLt);

    @Query("""
        SELECT new org.example.planner_backend.dto.ingredient.IngredientDetailResponseDto(
            i.id, i.nameLt, i.unit, CAST(COUNT(ri) AS int)
        )
        FROM Ingredient i
        LEFT JOIN RecipeIngredient ri ON ri.ingredient = i
        WHERE (:search IS NULL OR LOWER(i.nameLt) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        GROUP BY i.id, i.nameLt, i.unit
        ORDER BY i.nameLt
        """)
    List<IngredientDetailResponseDto> findAllWithRecipeCount(String search);

    Boolean existsByNameLtIgnoreCase(String nameLT);

    @Query("SELECT COUNT(ri) FROM RecipeIngredient ri WHERE ri.ingredient.id = :ingredientId")
    int countRecipesByIngredientId(UUID ingredientId);

    List<Ingredient> findByNameLtIgnoreCaseStartingWith(String prefix);
}

