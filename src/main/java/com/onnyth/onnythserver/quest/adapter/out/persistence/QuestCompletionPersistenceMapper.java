package com.onnyth.onnythserver.quest.adapter.out.persistence;

import com.onnyth.onnythserver.quest.domain.model.QuestCompletion;

public final class QuestCompletionPersistenceMapper {

    private QuestCompletionPersistenceMapper() {
    }

    public static QuestCompletion toDomain(QuestCompletionEntity entity) {
        if (entity == null) {
            return null;
        }
        return QuestCompletion.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .questId(entity.getQuestId())
                .completedAt(entity.getCompletedAt())
                .build();
    }

    public static QuestCompletionEntity toEntity(QuestCompletion domain) {
        if (domain == null) {
            return null;
        }
        return QuestCompletionEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .questId(domain.getQuestId())
                .completedAt(domain.getCompletedAt())
                .build();
    }
}
