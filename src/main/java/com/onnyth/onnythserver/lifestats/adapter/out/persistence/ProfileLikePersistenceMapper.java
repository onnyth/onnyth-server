package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.ProfileLike;
import com.onnyth.onnythserver.lifestats.domain.model.ProfileLikeId;

public final class ProfileLikePersistenceMapper {

    private ProfileLikePersistenceMapper() {
    }

    public static ProfileLike toDomain(ProfileLikeEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProfileLike.builder()
                .id(toDomainId(entity.getId()))
                .likerId(entity.getId() != null ? entity.getId().getLikerId() : null)
                .likedId(entity.getId() != null ? entity.getId().getLikedId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static ProfileLikeEntity toEntity(ProfileLike domain) {
        if (domain == null) {
            return null;
        }
        ProfileLikeEntity entity = new ProfileLikeEntity();
        entity.setId(toEntityId(resolveId(domain)));
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public static ProfileLikeId toDomainId(ProfileLikeEntityId entityId) {
        if (entityId == null) {
            return null;
        }
        return ProfileLikeId.builder()
                .likerId(entityId.getLikerId())
                .likedId(entityId.getLikedId())
                .build();
    }

    public static ProfileLikeEntityId toEntityId(ProfileLikeId domainId) {
        if (domainId == null) {
            return null;
        }
        ProfileLikeEntityId entityId = new ProfileLikeEntityId();
        entityId.setLikerId(domainId.getLikerId());
        entityId.setLikedId(domainId.getLikedId());
        return entityId;
    }

    private static ProfileLikeId resolveId(ProfileLike domain) {
        if (domain.getId() != null) {
            return domain.getId();
        }
        return ProfileLikeId.builder()
                .likerId(domain.getLikerId())
                .likedId(domain.getLikedId())
                .build();
    }
}
