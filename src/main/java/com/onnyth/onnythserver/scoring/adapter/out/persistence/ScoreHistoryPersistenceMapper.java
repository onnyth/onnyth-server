package com.onnyth.onnythserver.scoring.adapter.out.persistence;

import com.onnyth.onnythserver.scoring.domain.model.ScoreHistory;

public final class ScoreHistoryPersistenceMapper {

    private ScoreHistoryPersistenceMapper() {
    }

    public static ScoreHistory toDomain(ScoreHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return ScoreHistory.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .domain(entity.getDomain())
                .oldScore(entity.getOldScore())
                .newScore(entity.getNewScore())
                .reason(entity.getReason())
                .changedAt(entity.getChangedAt())
                .build();
    }

    public static ScoreHistoryEntity toEntity(ScoreHistory domain) {
        if (domain == null) {
            return null;
        }
        return ScoreHistoryEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .domain(domain.getDomain())
                .oldScore(domain.getOldScore())
                .newScore(domain.getNewScore())
                .reason(domain.getReason())
                .changedAt(domain.getChangedAt())
                .build();
    }
}
