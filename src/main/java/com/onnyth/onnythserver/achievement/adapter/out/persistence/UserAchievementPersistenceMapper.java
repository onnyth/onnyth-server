package com.onnyth.onnythserver.achievement.adapter.out.persistence;

import com.onnyth.onnythserver.achievement.domain.model.UserAchievement;

public final class UserAchievementPersistenceMapper {

    private UserAchievementPersistenceMapper() {
    }

    public static UserAchievement toDomain(UserAchievementEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserAchievement.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .achievementId(entity.getAchievementId())
                .unlockedAt(entity.getUnlockedAt())
                .build();
    }

    public static UserAchievementEntity toEntity(UserAchievement domain) {
        if (domain == null) {
            return null;
        }
        return UserAchievementEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .achievementId(domain.getAchievementId())
                .unlockedAt(domain.getUnlockedAt())
                .build();
    }
}
