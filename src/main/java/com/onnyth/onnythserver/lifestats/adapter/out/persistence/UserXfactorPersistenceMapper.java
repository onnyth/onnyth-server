package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.UserXfactor;

public final class UserXfactorPersistenceMapper {

    private UserXfactorPersistenceMapper() {
    }

    public static UserXfactor toDomain(UserXfactorEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserXfactor.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .evidenceUrl(entity.getEvidenceUrl())
                .metricValue(entity.getMetricValue())
                .metricLabel(entity.getMetricLabel())
                .isVerified(entity.getIsVerified())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static UserXfactorEntity toEntity(UserXfactor domain) {
        if (domain == null) {
            return null;
        }
        return UserXfactorEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .type(domain.getType())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .evidenceUrl(domain.getEvidenceUrl())
                .metricValue(domain.getMetricValue())
                .metricLabel(domain.getMetricLabel())
                .isVerified(domain.getIsVerified())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
