package org.example.planner_backend.repository;

import org.example.planner_backend.model.entity.MealPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, UUID> {

    @EntityGraph(attributePaths = {"slots", "slots.recipe"})
    Optional<MealPlan> findByFamilyIdAndStartDate(UUID familyId, LocalDate startDate);

    @Override
    @EntityGraph(attributePaths = {"slots", "slots.recipe"})
    @SuppressWarnings("NullableProblems")
    Optional<MealPlan> findById(UUID id);
}
