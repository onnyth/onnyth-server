package com.onnyth.onnythserver.registration.adapter.out.persistence;

import com.onnyth.onnythserver.registration.application.port.RegistrationDraftRepository;
import com.onnyth.onnythserver.registration.domain.model.RegistrationDraft;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RegistrationDraftRepositoryAdapter implements RegistrationDraftRepository {

    private final RegistrationDraftJpaRepository registrationDraftJpaRepository;

    @Override
    public RegistrationDraft save(RegistrationDraft draft) {
        RegistrationDraftEntity saved = registrationDraftJpaRepository.save(RegistrationDraftPersistenceMapper.toEntity(draft));
        return RegistrationDraftPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<RegistrationDraft> findById(UUID userId) {
        return registrationDraftJpaRepository.findById(userId)
                .map(RegistrationDraftPersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(UUID userId) {
        registrationDraftJpaRepository.deleteById(userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        registrationDraftJpaRepository.deleteByUserId(userId);
    }

    @Override
    public int deleteExpiredDrafts(Instant now) {
        return registrationDraftJpaRepository.deleteExpiredDrafts(now);
    }
}
