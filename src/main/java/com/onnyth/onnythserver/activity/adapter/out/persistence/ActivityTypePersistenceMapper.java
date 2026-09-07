package com.onnyth.onnythserver.activity.adapter.out.persistence;

import com.onnyth.onnythserver.activity.domain.model.ActivityType;

public final class ActivityTypePersistenceMapper {

    private ActivityTypePersistenceMapper() {
    }

    public static ActivityType toDomain(ActivityTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return ActivityType.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .icon(entity.getIcon())
                .category(entity.getCategory())
                .xpReward(entity.getXpReward())
                .frequency(entity.getFrequency())
                .cooldownHours(entity.getCooldownHours())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static ActivityTypeEntity toEntity(ActivityType domain) {
        if (domain == null) {
            return null;
        }
        return ActivityTypeEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .icon(domain.getIcon())
                .category(domain.getCategory())
                .xpReward(domain.getXpReward())
                .frequency(domain.getFrequency())
                .cooldownHours(domain.getCooldownHours())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
