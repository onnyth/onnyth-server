package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.UserOccupation;

public final class UserOccupationPersistenceMapper {

    private UserOccupationPersistenceMapper() {
    }

    public static UserOccupation toDomain(UserOccupationEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserOccupation.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .jobTitle(entity.getJobTitle())
                .rawJobTitle(entity.getRawJobTitle())
                .companyName(entity.getCompanyName())
                .rawCompanyName(entity.getRawCompanyName())
                .isVerified(entity.getIsVerified())
                .industry(entity.getIndustry())
                .employmentType(entity.getEmploymentType())
                .yearsExperience(entity.getYearsExperience())
                .skills(entity.getSkills())
                .isCurrent(entity.getIsCurrent())
                .score(entity.getScore())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserOccupationEntity toEntity(UserOccupation domain) {
        if (domain == null) {
            return null;
        }
        return UserOccupationEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .jobTitle(domain.getJobTitle())
                .rawJobTitle(domain.getRawJobTitle())
                .companyName(domain.getCompanyName())
                .rawCompanyName(domain.getRawCompanyName())
                .isVerified(domain.getIsVerified())
                .industry(domain.getIndustry())
                .employmentType(domain.getEmploymentType())
                .yearsExperience(domain.getYearsExperience())
                .skills(domain.getSkills())
                .isCurrent(domain.getIsCurrent())
                .score(domain.getScore())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
