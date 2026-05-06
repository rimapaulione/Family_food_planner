package org.example.planner_backend.repository;


import org.example.planner_backend.model.entity.Recipe;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    @EntityGraph(attributePaths = {"tags"})
    List<Recipe> findByFamilyId(UUID familyId);

    @EntityGraph(attributePaths = {"tags"})
    List<Recipe> findByFamilyIdAndNameContainingIgnoreCase(UUID familyId, String search);

    Optional<Recipe> findByIdAndFamilyId(UUID id, UUID familyId);
}
