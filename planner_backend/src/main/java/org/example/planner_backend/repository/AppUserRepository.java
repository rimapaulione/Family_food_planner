package org.example.planner_backend.repository;

import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AppUser> findByFamilyId(UUID familyId);

    Long countByFamilyIdAndRole(UUID familyId, Role role);
}
