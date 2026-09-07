package com.onnyth.onnythserver.registration.adapter.out.persistence;

import com.onnyth.onnythserver.registration.domain.model.RegistrationDraft;

public final class RegistrationDraftPersistenceMapper {

    private RegistrationDraftPersistenceMapper() {
    }

    public static RegistrationDraft toDomain(RegistrationDraftEntity entity) {
        if (entity == null) {
            return null;
        }
        return RegistrationDraft.builder()
                .userId(entity.getUserId())
                .currentStep(entity.getCurrentStep())
                .draftData(entity.getDraftData())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    public static RegistrationDraftEntity toEntity(RegistrationDraft domain) {
        if (domain == null) {
            return null;
        }
        return RegistrationDraftEntity.builder()
                .userId(domain.getUserId())
                .currentStep(domain.getCurrentStep())
                .draftData(domain.getDraftData())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .expiresAt(domain.getExpiresAt())
                .build();
    }
}
