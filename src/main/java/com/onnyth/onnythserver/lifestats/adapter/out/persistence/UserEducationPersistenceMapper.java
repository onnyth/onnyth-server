package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.UserEducation;

public final class UserEducationPersistenceMapper {

    private UserEducationPersistenceMapper() {
    }

    public static UserEducation toDomain(UserEducationEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserEducation.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .level(entity.getLevel())
                .institution(entity.getInstitution())
                .fieldOfStudy(entity.getFieldOfStudy())
                .graduationYear(entity.getGraduationYear())
                .isHighest(entity.getIsHighest())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static UserEducationEntity toEntity(UserEducation domain) {
        if (domain == null) {
            return null;
        }
        return UserEducationEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .level(domain.getLevel())
                .institution(domain.getInstitution())
                .fieldOfStudy(domain.getFieldOfStudy())
                .graduationYear(domain.getGraduationYear())
                .isHighest(domain.getIsHighest())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
