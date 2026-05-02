package org.example.planner_backend.repository;

import org.example.planner_backend.model.entity.MealSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MealSlotRepository extends JpaRepository<MealSlot, UUID> {
}
