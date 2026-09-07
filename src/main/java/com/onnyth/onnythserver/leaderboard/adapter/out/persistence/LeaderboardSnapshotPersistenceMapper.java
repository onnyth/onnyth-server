package com.onnyth.onnythserver.leaderboard.adapter.out.persistence;

import com.onnyth.onnythserver.leaderboard.domain.model.LeaderboardSnapshot;

public final class LeaderboardSnapshotPersistenceMapper {

    private LeaderboardSnapshotPersistenceMapper() {
    }

    public static LeaderboardSnapshot toDomain(LeaderboardSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }
        return LeaderboardSnapshot.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .friendOwnerId(entity.getFriendOwnerId())
                .position(entity.getPosition())
                .score(entity.getScore())
                .snapshotDate(entity.getSnapshotDate())
                .category(entity.getCategory())
                .build();
    }

    public static LeaderboardSnapshotEntity toEntity(LeaderboardSnapshot domain) {
        if (domain == null) {
            return null;
        }
        return LeaderboardSnapshotEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .friendOwnerId(domain.getFriendOwnerId())
                .position(domain.getPosition())
                .score(domain.getScore())
                .snapshotDate(domain.getSnapshotDate())
                .category(domain.getCategory())
                .build();
    }
}
