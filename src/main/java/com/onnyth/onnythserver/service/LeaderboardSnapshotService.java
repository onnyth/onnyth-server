package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.models.LeaderboardSnapshot;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.FriendshipRepository;
import com.onnyth.onnythserver.repository.LeaderboardSnapshotRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardSnapshotService {

    private final LeaderboardSnapshotRepository snapshotRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * Take a weekly snapshot of leaderboard positions every Sunday at midnight.
     */
    @Scheduled(cron = "0 0 0 * * SUN")
    @Transactional
    public void takeWeeklySnapshot() {
        log.info("Starting weekly leaderboard snapshot...");
        LocalDate today = LocalDate.now();
        List<User> allUsers = userRepository.findAll();

        int snapshotCount = 0;
        for (User owner : allUsers) {
            List<UUID> friendIds = friendshipRepository.findFriendIdsByUserId(owner.getId());
            if (friendIds.isEmpty())
                continue;

            List<UUID> participantIds = new ArrayList<>(friendIds);
            participantIds.add(owner.getId());

            List<User> participants = userRepository.findAllById(participantIds);
            participants.sort(Comparator.comparingLong(User::getTotalScore).reversed());

            for (int i = 0; i < participants.size(); i++) {
                User participant = participants.get(i);
                LeaderboardSnapshot snapshot = LeaderboardSnapshot.builder()
                        .userId(participant.getId())
                        .friendOwnerId(owner.getId())
                        .position(i + 1)
                        .score(participant.getTotalScore())
                        .snapshotDate(today)
                        .build();
                snapshotRepository.save(snapshot);
                snapshotCount++;
            }
        }
        log.info("Weekly snapshot complete — {} snapshots saved", snapshotCount);
    }

    /**
     * Get position changes compared to last week's snapshot.
     * Positive = moved up, negative = moved down, null = no change data.
     */
    @Transactional(readOnly = true)
    public Map<UUID, Integer> getPositionChanges(UUID friendOwnerId, List<UUID> participantIds) {
        LocalDate lastSunday = getLastSunday();
        List<LeaderboardSnapshot> snapshots = snapshotRepository
                .findByFriendOwnerIdAndSnapshotDate(friendOwnerId, lastSunday);

        if (snapshots.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Integer> oldPositions = snapshots.stream()
                .collect(Collectors.toMap(LeaderboardSnapshot::getUserId, LeaderboardSnapshot::getPosition));

        // Compute current positions
        List<User> participants = userRepository.findAllById(participantIds);
        participants.sort(Comparator.comparingLong(User::getTotalScore).reversed());

        Map<UUID, Integer> changes = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            UUID uid = participants.get(i).getId();
            int currentPosition = i + 1;
            Integer oldPosition = oldPositions.get(uid);
            if (oldPosition != null) {
                // positive change = moved up (old position was higher number)
                changes.put(uid, oldPosition - currentPosition);
            }
        }

        return changes;
    }

    /**
     * Get the set of user IDs that appeared in last week's snapshot.
     * Used to determine which users are "new" on the leaderboard.
     */
    @Transactional(readOnly = true)
    public Set<UUID> getSnapshotUserIds(UUID friendOwnerId) {
        LocalDate lastSunday = getLastSunday();
        List<LeaderboardSnapshot> snapshots = snapshotRepository
                .findByFriendOwnerIdAndSnapshotDate(friendOwnerId, lastSunday);

        if (snapshots.isEmpty()) {
            return Set.of();
        }

        return snapshots.stream()
                .map(LeaderboardSnapshot::getUserId)
                .collect(Collectors.toSet());
    }

    private LocalDate getLastSunday() {
        LocalDate today = LocalDate.now();
        return today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
    }
}
