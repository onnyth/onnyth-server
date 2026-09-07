package com.onnyth.onnythserver.streak.adapter.out.persistence;

import com.onnyth.onnythserver.streak.domain.model.UserStreak;

public final class UserStreakPersistenceMapper {

    private UserStreakPersistenceMapper() {
    }

    public static UserStreak toDomain(UserStreakEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserStreak.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .currentStreak(entity.getCurrentStreak())
                .longestStreak(entity.getLongestStreak())
                .lastActivityDate(entity.getLastActivityDate())
                .build();
    }

    public static UserStreakEntity toEntity(UserStreak domain) {
        if (domain == null) {
            return null;
        }
        return UserStreakEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .currentStreak(domain.getCurrentStreak())
                .longestStreak(domain.getLongestStreak())
                .lastActivityDate(domain.getLastActivityDate())
                .build();
    }
}
