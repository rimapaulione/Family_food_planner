package org.example.planner_backend.repository;


import org.example.planner_backend.model.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.tags LEFT JOIN FETCH r.ingredients " +
            "WHERE r.family.id = :familyId")
    List<Recipe> findAllWithTagsAndIngredientsByFamilyId(@Param("familyId") UUID familyId);

    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.tags LEFT JOIN FETCH r.ingredients " +
            "WHERE r.family.id = :familyId AND LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Recipe> findAllWithTagsAndIngredientsByFamilyIdAndSearch(
            @Param("familyId") UUID familyId,
            @Param("search") String search);

    Optional<Recipe> findByIdAndFamilyId(UUID id, UUID familyId);
}
