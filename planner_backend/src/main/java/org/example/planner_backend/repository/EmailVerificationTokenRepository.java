package org.example.planner_backend.repository;

import org.example.planner_backend.model.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    void deleteByUserId(UUID id);

    @Modifying
    @Query("""
          UPDATE EmailVerificationToken t
             SET t.consumedAt = :now
           WHERE t.tokenHash = :tokenHash AND t.consumedAt IS NULL AND t.expiresAt > :now
          """)
    int consumeIfValid(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt <:cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
