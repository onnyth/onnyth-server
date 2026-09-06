package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.UserPhysique;

public final class UserPhysiquePersistenceMapper {

    private UserPhysiquePersistenceMapper() {
    }

    public static UserPhysique toDomain(UserPhysiqueEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserPhysique.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .heightCm(entity.getHeightCm())
                .weightKg(entity.getWeightKg())
                .bodyFatPct(entity.getBodyFatPct())
                .fitnessLevel(entity.getFitnessLevel())
                .workoutSource(entity.getWorkoutSource())
                .weeklyWorkouts(entity.getWeeklyWorkouts())
                .score(entity.getScore())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserPhysiqueEntity toEntity(UserPhysique domain) {
        if (domain == null) {
            return null;
        }
        return UserPhysiqueEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .heightCm(domain.getHeightCm())
                .weightKg(domain.getWeightKg())
                .bodyFatPct(domain.getBodyFatPct())
                .fitnessLevel(domain.getFitnessLevel())
                .workoutSource(domain.getWorkoutSource())
                .weeklyWorkouts(domain.getWeeklyWorkouts())
                .score(domain.getScore())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
