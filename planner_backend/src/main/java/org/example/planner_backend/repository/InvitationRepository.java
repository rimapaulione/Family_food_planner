package org.example.planner_backend.repository;


import org.example.planner_backend.model.entity.FamilyInvitation;
import org.example.planner_backend.model.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<FamilyInvitation, UUID> {

    boolean existsByFamilyIdAndInvitedEmailAndStatus(UUID familyId, String invitedEmail, InvitationStatus status);

    List<FamilyInvitation> findByFamilyIdAndStatus(UUID familyId, InvitationStatus status);

    Optional<FamilyInvitation> findByToken(String token);
}
