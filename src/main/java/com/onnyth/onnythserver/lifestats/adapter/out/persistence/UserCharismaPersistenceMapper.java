package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.UserCharisma;

public final class UserCharismaPersistenceMapper {

    private UserCharismaPersistenceMapper() {
    }

    public static UserCharisma toDomain(UserCharismaEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserCharisma.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .onnythProfileLikes(entity.getOnnythProfileLikes())
                .score(entity.getScore())
                .lastSocialSyncAt(entity.getLastSocialSyncAt())
                .relationshipStatus(entity.getRelationshipStatus())
                .socialCircleSize(entity.getSocialCircleSize())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserCharismaEntity toEntity(UserCharisma domain) {
        if (domain == null) {
            return null;
        }
        return UserCharismaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .onnythProfileLikes(domain.getOnnythProfileLikes())
                .score(domain.getScore())
                .lastSocialSyncAt(domain.getLastSocialSyncAt())
                .relationshipStatus(domain.getRelationshipStatus())
                .socialCircleSize(domain.getSocialCircleSize())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
