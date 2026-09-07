package com.onnyth.onnythserver.achievement.adapter.out.persistence;

import com.onnyth.onnythserver.achievement.domain.model.Achievement;

public final class AchievementPersistenceMapper {

    private AchievementPersistenceMapper() {
    }

    public static Achievement toDomain(AchievementEntity entity) {
        if (entity == null) {
            return null;
        }
        return Achievement.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .icon(entity.getIcon())
                .category(entity.getCategory())
                .requirementType(entity.getRequirementType())
                .threshold(entity.getThreshold())
                .points(entity.getPoints())
                .isActive(entity.isActive())
                .build();
    }

    public static AchievementEntity toEntity(Achievement domain) {
        if (domain == null) {
            return null;
        }
        return AchievementEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .icon(domain.getIcon())
                .category(domain.getCategory())
                .requirementType(domain.getRequirementType())
                .threshold(domain.getThreshold())
                .points(domain.getPoints())
                .isActive(domain.isActive())
                .build();
    }
}
