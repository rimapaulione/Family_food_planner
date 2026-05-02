package org.example.planner_backend.repository;

import org.example.planner_backend.model.entity.MealSlot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealSlotRepository extends JpaRepository<MealSlot, UUID> {

    @Override
    @EntityGraph(attributePaths = {"mealPlan", "mealPlan.family", "recipe"})
    @SuppressWarnings("NullableProblems")
    Optional<MealSlot> findById(UUID id);
}
