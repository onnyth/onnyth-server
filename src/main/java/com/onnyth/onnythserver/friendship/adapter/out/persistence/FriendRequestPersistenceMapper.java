package com.onnyth.onnythserver.friendship.adapter.out.persistence;

import com.onnyth.onnythserver.friendship.domain.model.FriendRequest;

public final class FriendRequestPersistenceMapper {

    private FriendRequestPersistenceMapper() {
    }

    public static FriendRequest toDomain(FriendRequestEntity entity) {
        if (entity == null) {
            return null;
        }
        return FriendRequest.builder()
                .id(entity.getId())
                .senderId(entity.getSenderId())
                .receiverId(entity.getReceiverId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static FriendRequestEntity toEntity(FriendRequest domain) {
        if (domain == null) {
            return null;
        }
        return FriendRequestEntity.builder()
                .id(domain.getId())
                .senderId(domain.getSenderId())
                .receiverId(domain.getReceiverId())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
