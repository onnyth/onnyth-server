package com.onnyth.onnythserver.registration.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface RegistrationDraftJpaRepository extends JpaRepository<RegistrationDraftEntity, UUID> {

    @Modifying
    @Query("DELETE FROM RegistrationDraftEntity d WHERE d.expiresAt < :now")
    int deleteExpiredDrafts(Instant now);

    void deleteByUserId(UUID userId);
}
