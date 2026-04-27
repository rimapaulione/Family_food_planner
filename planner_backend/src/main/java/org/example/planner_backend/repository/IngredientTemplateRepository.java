package org.example.planner_backend.repository;

import org.example.planner_backend.model.entity.IngredientTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngredientTemplateRepository extends JpaRepository<IngredientTemplate, UUID> {
}
