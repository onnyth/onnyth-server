package com.onnyth.onnythserver.feed.adapter.out.persistence;

import com.onnyth.onnythserver.feed.domain.model.FeedEvent;

public final class FeedEventPersistenceMapper {

    private FeedEventPersistenceMapper() {
    }

    public static FeedEvent toDomain(FeedEventEntity entity) {
        if (entity == null) {
            return null;
        }
        return FeedEvent.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .eventType(entity.getEventType())
                .eventData(entity.getEventData())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static FeedEventEntity toEntity(FeedEvent domain) {
        if (domain == null) {
            return null;
        }
        return FeedEventEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .eventType(domain.getEventType())
                .eventData(domain.getEventData())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
