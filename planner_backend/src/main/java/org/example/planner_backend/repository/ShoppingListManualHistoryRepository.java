package org.example.planner_backend.repository;

import org.example.planner_backend.model.entity.ShoppingListManualHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShoppingListManualHistoryRepository extends JpaRepository<ShoppingListManualHistory, UUID> {

    List<ShoppingListManualHistory> findByFamilyIdAndWeekStart(UUID familyId, LocalDate weekStart);
}
