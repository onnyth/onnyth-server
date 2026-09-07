package com.onnyth.onnythserver.quest.adapter.out.persistence;

import com.onnyth.onnythserver.quest.domain.model.Quest;

public final class QuestPersistenceMapper {

    private QuestPersistenceMapper() {
    }

    public static Quest toDomain(QuestEntity entity) {
        if (entity == null) {
            return null;
        }
        return Quest.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .xpReward(entity.getXpReward())
                .category(entity.getCategory())
                .status(entity.getStatus())
                .deadline(entity.getDeadline())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static QuestEntity toEntity(Quest domain) {
        if (domain == null) {
            return null;
        }
        return QuestEntity.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .xpReward(domain.getXpReward())
                .category(domain.getCategory())
                .status(domain.getStatus())
                .deadline(domain.getDeadline())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
