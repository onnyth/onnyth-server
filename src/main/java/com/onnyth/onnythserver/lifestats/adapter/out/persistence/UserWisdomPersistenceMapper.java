package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.UserWisdom;

public final class UserWisdomPersistenceMapper {

    private UserWisdomPersistenceMapper() {
    }

    public static UserWisdom toDomain(UserWisdomEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserWisdom.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .habitIds(entity.getHabitIds())
                .languages(entity.getLanguages())
                .educationLevel(entity.getEducationLevel())
                .institutionName(entity.getInstitutionName())
                .graduationYear(entity.getGraduationYear())
                .score(entity.getScore())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserWisdomEntity toEntity(UserWisdom domain) {
        if (domain == null) {
            return null;
        }
        return UserWisdomEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .habitIds(domain.getHabitIds())
                .languages(domain.getLanguages())
                .educationLevel(domain.getEducationLevel())
                .institutionName(domain.getInstitutionName())
                .graduationYear(domain.getGraduationYear())
                .score(domain.getScore())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
