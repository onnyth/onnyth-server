package com.onnyth.onnythserver.leaderboard.application.port;

import com.onnyth.onnythserver.leaderboard.domain.model.LeaderboardSnapshot;
import com.onnyth.onnythserver.models.StatDomain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaderboardSnapshotRepository {

    LeaderboardSnapshot save(LeaderboardSnapshot leaderboardSnapshot);

    List<LeaderboardSnapshot> findByFriendOwnerIdAndSnapshotDate(UUID friendOwnerId, LocalDate snapshotDate);

    List<LeaderboardSnapshot> findByFriendOwnerIdAndSnapshotDateAndCategory(
            UUID friendOwnerId, LocalDate snapshotDate, StatDomain category);
}
