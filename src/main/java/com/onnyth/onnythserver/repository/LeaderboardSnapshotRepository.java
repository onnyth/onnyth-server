package com.onnyth.onnythserver.repository;

import com.onnyth.onnythserver.models.LeaderboardSnapshot;
import com.onnyth.onnythserver.models.StatCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaderboardSnapshotRepository extends JpaRepository<LeaderboardSnapshot, UUID> {

    List<LeaderboardSnapshot> findByFriendOwnerIdAndSnapshotDate(UUID friendOwnerId, LocalDate snapshotDate);

    List<LeaderboardSnapshot> findByFriendOwnerIdAndSnapshotDateAndCategory(
            UUID friendOwnerId, LocalDate snapshotDate, StatCategory category);
}
