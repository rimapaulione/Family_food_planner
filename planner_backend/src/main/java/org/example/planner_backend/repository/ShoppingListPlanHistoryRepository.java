package org.example.planner_backend.repository;

import org.example.planner_backend.model.entity.ShoppingListPlanHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingListPlanHistoryRepository extends JpaRepository<ShoppingListPlanHistory, UUID> {

    @EntityGraph(attributePaths = "ingredient")
    List<ShoppingListPlanHistory> findByFamilyIdAndWeekStart(UUID familyId, LocalDate weekStart);

    Optional<ShoppingListPlanHistory> findByFamilyIdAndWeekStartAndIngredientId(
            UUID familyId, LocalDate weekStart, UUID ingredientId);
}
