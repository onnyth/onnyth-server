package com.onnyth.onnythserver.leaderboard.adapter.out.persistence;

import com.onnyth.onnythserver.models.StatDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaderboardSnapshotJpaRepository extends JpaRepository<LeaderboardSnapshotEntity, UUID> {

    List<LeaderboardSnapshotEntity> findByFriendOwnerIdAndSnapshotDate(UUID friendOwnerId, LocalDate snapshotDate);

    List<LeaderboardSnapshotEntity> findByFriendOwnerIdAndSnapshotDateAndCategory(
            UUID friendOwnerId, LocalDate snapshotDate, StatDomain category);
}
