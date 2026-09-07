package com.onnyth.onnythserver.leaderboard.application.usecase;

import com.onnyth.onnythserver.leaderboard.application.port.LeaderboardSnapshotRepository;
import com.onnyth.onnythserver.leaderboard.domain.model.LeaderboardSnapshot;
import com.onnyth.onnythserver.lifestats.application.port.UserCharismaRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserOccupationRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserPhysiqueRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserWealthRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserWisdomRepository;
import com.onnyth.onnythserver.models.StatDomain;
import com.onnyth.onnythserver.friendship.application.port.FriendshipRepository;
import com.onnyth.onnythserver.user.application.port.UserRepository;
import com.onnyth.onnythserver.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardSnapshotUseCaseService {

    private final LeaderboardSnapshotRepository snapshotRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserOccupationRepository occupationRepository;
    private final UserWealthRepository wealthRepository;
    private final UserPhysiqueRepository physiqueRepository;
    private final UserWisdomRepository wisdomRepository;
    private final UserCharismaRepository charismaRepository;

    /**
     * Take a weekly snapshot of leaderboard positions every Sunday at midnight.
     * Captures both overall rankings and per-domain rankings.
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
            if (friendIds.isEmpty()) {
                continue;
            }

            List<UUID> participantIds = new ArrayList<>(friendIds);
            participantIds.add(owner.getId());

            List<User> participants = userRepository.findAllById(participantIds);

            participants.sort(Comparator.comparingLong(User::getTotalScore).reversed());
            for (int i = 0; i < participants.size(); i++) {
                User participant = participants.get(i);
                snapshotRepository.save(LeaderboardSnapshot.builder()
                        .userId(participant.getId())
                        .friendOwnerId(owner.getId())
                        .position(i + 1)
                        .score(participant.getTotalScore())
                        .snapshotDate(today)
                        .category(null)
                        .build());
                snapshotCount++;
            }

            Map<UUID, User> usersById = participants.stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));

            for (StatDomain domain : StatDomain.values()) {
                Map<UUID, Integer> scoreMap = new HashMap<>();
                for (UUID pid : participantIds) {
                    scoreMap.put(pid, getDomainScore(pid, domain));
                }

                List<Map.Entry<UUID, Integer>> ranked = new ArrayList<>(scoreMap.entrySet());
                ranked.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

                for (int i = 0; i < ranked.size(); i++) {
                    Map.Entry<UUID, Integer> entry = ranked.get(i);
                    if (!usersById.containsKey(entry.getKey())) {
                        continue;
                    }
                    snapshotRepository.save(LeaderboardSnapshot.builder()
                            .userId(entry.getKey())
                            .friendOwnerId(owner.getId())
                            .position(i + 1)
                            .score(entry.getValue())
                            .snapshotDate(today)
                            .category(domain)
                            .build());
                    snapshotCount++;
                }
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
                .filter(snapshot -> snapshot.getCategory() == null)
                .collect(Collectors.toMap(LeaderboardSnapshot::getUserId, LeaderboardSnapshot::getPosition));

        List<User> participants = userRepository.findAllById(participantIds);
        participants.sort(Comparator.comparingLong(User::getTotalScore).reversed());

        Map<UUID, Integer> changes = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            UUID uid = participants.get(i).getId();
            int currentPosition = i + 1;
            Integer oldPosition = oldPositions.get(uid);
            if (oldPosition != null) {
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
                .filter(snapshot -> snapshot.getCategory() == null)
                .map(LeaderboardSnapshot::getUserId)
                .collect(Collectors.toSet());
    }

    private int getDomainScore(UUID userId, StatDomain domain) {
        return switch (domain) {
            case OCCUPATION -> occupationRepository.findByUserIdAndIsCurrentTrue(userId)
                    .map(occupation -> occupation.getScore()).orElse(0);
            case WEALTH -> wealthRepository.findByUserId(userId)
                    .map(wealth -> wealth.getScore()).orElse(0);
            case PHYSIQUE -> physiqueRepository.findByUserId(userId)
                    .map(physique -> physique.getScore()).orElse(0);
            case WISDOM -> wisdomRepository.findByUserId(userId)
                    .map(wisdom -> wisdom.getScore()).orElse(0);
            case CHARISMA -> charismaRepository.findByUserId(userId)
                    .map(charisma -> charisma.getScore()).orElse(0);
        };
    }

    private LocalDate getLastSunday() {
        LocalDate today = LocalDate.now();
        return today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
    }
}
