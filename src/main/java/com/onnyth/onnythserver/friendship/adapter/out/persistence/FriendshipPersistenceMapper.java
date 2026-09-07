package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import com.onnyth.onnythserver.friendship.domain.model.Friendship;

public final class FriendshipPersistenceMapper {

    private FriendshipPersistenceMapper() {
    }

    public static Friendship toDomain(FriendshipEntity entity) {
        if (entity == null) {
            return null;
        }
        return Friendship.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .friendId(entity.getFriendId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static FriendshipEntity toEntity(Friendship domain) {
        if (domain == null) {
            return null;
        }
        return FriendshipEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .friendId(domain.getFriendId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
