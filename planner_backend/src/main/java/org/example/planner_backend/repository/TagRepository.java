package org.example.planner_backend.repository;

import org.example.planner_backend.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Set<Tag> findByIdIn(Set<Long> ids);
}
