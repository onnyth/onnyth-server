package com.onnyth.onnythserver.registration.application.port;

import com.onnyth.onnythserver.registration.domain.model.RegistrationDraft;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationDraftRepository {

    RegistrationDraft save(RegistrationDraft draft);

    Optional<RegistrationDraft> findById(UUID userId);

    void deleteById(UUID userId);

    void deleteByUserId(UUID userId);

    int deleteExpiredDrafts(Instant now);
}
