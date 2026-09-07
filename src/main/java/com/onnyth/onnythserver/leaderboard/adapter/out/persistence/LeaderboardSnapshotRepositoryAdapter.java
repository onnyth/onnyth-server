package com.onnyth.onnythserver.leaderboard.adapter.out.persistence;

import com.onnyth.onnythserver.leaderboard.application.port.LeaderboardSnapshotRepository;
import com.onnyth.onnythserver.leaderboard.domain.model.LeaderboardSnapshot;
import com.onnyth.onnythserver.models.StatDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class LeaderboardSnapshotRepositoryAdapter implements LeaderboardSnapshotRepository {

    private final LeaderboardSnapshotJpaRepository leaderboardSnapshotJpaRepository;

    @Override
    public LeaderboardSnapshot save(LeaderboardSnapshot leaderboardSnapshot) {
        LeaderboardSnapshotEntity saved = leaderboardSnapshotJpaRepository
                .save(LeaderboardSnapshotPersistenceMapper.toEntity(leaderboardSnapshot));
        return LeaderboardSnapshotPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<LeaderboardSnapshot> findByFriendOwnerIdAndSnapshotDate(UUID friendOwnerId, LocalDate snapshotDate) {
        return leaderboardSnapshotJpaRepository.findByFriendOwnerIdAndSnapshotDate(friendOwnerId, snapshotDate)
                .stream()
                .map(LeaderboardSnapshotPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<LeaderboardSnapshot> findByFriendOwnerIdAndSnapshotDateAndCategory(
            UUID friendOwnerId, LocalDate snapshotDate, StatDomain category) {
        return leaderboardSnapshotJpaRepository.findByFriendOwnerIdAndSnapshotDateAndCategory(
                        friendOwnerId, snapshotDate, category)
                .stream()
                .map(LeaderboardSnapshotPersistenceMapper::toDomain)
                .toList();
    }
}
