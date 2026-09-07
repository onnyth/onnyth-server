package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import com.onnyth.onnythserver.friendship.domain.model.Follow;
import com.onnyth.onnythserver.friendship.domain.model.FollowId;
import com.onnyth.onnythserver.user.adapter.out.persistence.UserEntity;

public final class FollowPersistenceMapper {

    private FollowPersistenceMapper() {
    }

    public static Follow toDomain(FollowEntity entity) {
        if (entity == null) {
            return null;
        }
        return Follow.builder()
                .id(toDomainId(entity.getId()))
                .followerId(entity.getFollower() != null ? entity.getFollower().getId() : null)
                .followingId(entity.getFollowing() != null ? entity.getFollowing().getId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static FollowEntity toEntity(Follow domain) {
        if (domain == null) {
            return null;
        }
        FollowEntity entity = new FollowEntity();
        entity.setId(toEntityId(resolveId(domain)));
        entity.setFollower(toUserEntity(domain.getFollowerId()));
        entity.setFollowing(toUserEntity(domain.getFollowingId()));
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public static FollowId toDomainId(FollowEntityId entityId) {
        if (entityId == null) {
            return null;
        }
        return FollowId.builder()
                .followerId(entityId.getFollowerId())
                .followingId(entityId.getFollowingId())
                .build();
    }

    public static FollowEntityId toEntityId(FollowId domainId) {
        if (domainId == null) {
            return null;
        }
        FollowEntityId entityId = new FollowEntityId();
        entityId.setFollowerId(domainId.getFollowerId());
        entityId.setFollowingId(domainId.getFollowingId());
        return entityId;
    }

    private static FollowId resolveId(Follow domain) {
        if (domain.getId() != null) {
            return domain.getId();
        }
        return FollowId.builder()
                .followerId(domain.getFollowerId())
                .followingId(domain.getFollowingId())
                .build();
    }

    private static UserEntity toUserEntity(java.util.UUID userId) {
        if (userId == null) {
            return null;
        }
        return UserEntity.builder()
                .id(userId)
                .build();
    }
}
