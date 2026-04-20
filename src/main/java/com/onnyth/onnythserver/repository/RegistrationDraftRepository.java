package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.RegistrationDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface RegistrationDraftRepository extends JpaRepository<RegistrationDraft, UUID> {

    /**
     * Delete expired drafts (cleanup job).
     */
    @Modifying
    @Query("DELETE FROM RegistrationDraft d WHERE d.expiresAt < :now")
    int deleteExpiredDrafts(Instant now);
}
