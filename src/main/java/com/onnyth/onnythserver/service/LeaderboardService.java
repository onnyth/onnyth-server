package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.*;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.StatDomain;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.FriendshipRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import com.onnyth.onnythserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserOccupationRepository occupationRepository;
    private final UserWealthRepository wealthRepository;
    private final UserPhysiqueRepository physiqueRepository;
    private final UserWisdomRepository wisdomRepository;
    private final UserCharismaRepository charismaRepository;
    private final LeaderboardSnapshotService snapshotService;

    // ─── Friends Leaderboard (overall) ────────────────────────────────────────

    @Transactional(readOnly = true)
    public LeaderboardResponse getFriendsLeaderboard(UUID userId, Pageable pageable) {
        List<UUID> participantIds = getParticipantIds(userId);
        List<User> allParticipants = new ArrayList<>(userRepository.findAllById(participantIds));

        // Sort by totalScore DESC
        allParticipants.sort(Comparator.comparingLong(User::getTotalScore).reversed());

        // Find current user position and score across ALL participants
        int currentUserPosition = 0;
        long currentUserScore = 0;
        for (int i = 0; i < allParticipants.size(); i++) {
            if (allParticipants.get(i).getId().equals(userId)) {
                currentUserPosition = i + 1;
                currentUserScore = allParticipants.get(i).getTotalScore();
                break;
            }
        }

        // Get position changes from snapshot
        Map<UUID, Integer> positionChanges = snapshotService.getPositionChanges(userId, participantIds);
        Set<UUID> snapshotUserIds = snapshotService.getSnapshotUserIds(userId);

        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allParticipants.size());
        List<User> pageContent = start < allParticipants.size()
                ? allParticipants.subList(start, end)
                : List.of();

        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        for (int i = 0; i < pageContent.size(); i++) {
            User user = pageContent.get(i);
            int position = start + i + 1;
            UUID uid = user.getId();
            Integer change = positionChanges.getOrDefault(uid, null);
            boolean isNew = !snapshotUserIds.isEmpty() && !snapshotUserIds.contains(uid);

            entries.add(LeaderboardEntryResponse.builder()
                    .position(position)
                    .userId(uid)
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .profilePic(user.getProfilePic())
                    .totalScore(user.getTotalScore())
                    .rankTier(user.getRankTier() != null ? user.getRankTier().getDisplayName() : null)
                    .isCurrentUser(uid.equals(userId))
                    .positionChange(change)
                    .isNew(isNew)
                    .build());
        }

        return LeaderboardResponse.builder()
                .entries(entries)
                .totalFriends(allParticipants.size() - 1) // exclude self
                .currentUserPosition(currentUserPosition)
                .currentUserScore(currentUserScore)
                .build();
    }

    // ─── User Position ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserLeaderboardPositionResponse getUserPosition(UUID userId) {
        List<UUID> participantIds = getParticipantIds(userId);
        List<User> allParticipants = new ArrayList<>(userRepository.findAllById(participantIds));

        allParticipants.sort(Comparator.comparingLong(User::getTotalScore).reversed());

        int position = 0;
        long myScore = 0;
        for (int i = 0; i < allParticipants.size(); i++) {
            if (allParticipants.get(i).getId().equals(userId)) {
                position = i + 1;
                myScore = allParticipants.get(i).getTotalScore();
                break;
            }
        }

        // Find user ahead
        String userAheadUsername = null;
        UUID userAheadId = null;
        long pointsToNext = 0;

        if (position > 1) {
            User ahead = allParticipants.get(position - 2); // 0-indexed, position-1 is self, position-2 is ahead
            userAheadUsername = ahead.getUsername();
            userAheadId = ahead.getId();
            pointsToNext = ahead.getTotalScore() - myScore;
        }

        return UserLeaderboardPositionResponse.builder()
                .position(position)
                .totalParticipants(allParticipants.size())
                .score(myScore)
                .pointsToNextPosition(pointsToNext)
                .userAheadUsername(userAheadUsername)
                .userAheadId(userAheadId)
                .build();
    }

    // ─── Category Leaderboard ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CategoryLeaderboardEntryResponse> getLeaderboardByCategory(
            UUID userId, StatDomain domain, Pageable pageable) {

        List<UUID> participantIds = getParticipantIds(userId);
        List<User> allParticipants = userRepository.findAllById(participantIds);
        Map<UUID, User> usersById = allParticipants.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Get domain scores for all participants
        Map<UUID, Integer> scoreMap = new HashMap<>();
        for (UUID pid : participantIds) {
            scoreMap.put(pid, getDomainScore(pid, domain));
        }

        // Build sortable list by score DESC
        List<Map.Entry<UUID, Integer>> ranked = new ArrayList<>(scoreMap.entrySet());
        ranked.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // Paginate
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), ranked.size());
        List<Map.Entry<UUID, Integer>> pageContent = start < ranked.size()
                ? ranked.subList(start, end)
                : List.of();

        List<CategoryLeaderboardEntryResponse> entries = new ArrayList<>();
        for (int i = 0; i < pageContent.size(); i++) {
            Map.Entry<UUID, Integer> entry = pageContent.get(i);
            User user = usersById.get(entry.getKey());
            if (user == null)
                continue;

            entries.add(CategoryLeaderboardEntryResponse.builder()
                    .position(start + i + 1)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .profilePic(user.getProfilePic())
                    .categoryValue(entry.getValue())
                    .category(domain.getDisplayName())
                    .rankTier(user.getRankTier() != null ? user.getRankTier().getDisplayName() : null)
                    .isCurrentUser(user.getId().equals(userId))
                    .build());
        }

        return new PageImpl<>(entries, pageable, ranked.size());
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private int getDomainScore(UUID userId, StatDomain domain) {
        return switch (domain) {
            case OCCUPATION -> occupationRepository.findByUserIdAndIsCurrentTrue(userId)
                    .map(o -> o.getScore()).orElse(0);
            case WEALTH -> wealthRepository.findByUserId(userId)
                    .map(w -> w.getScore()).orElse(0);
            case PHYSIQUE -> physiqueRepository.findByUserId(userId)
                    .map(p -> p.getScore()).orElse(0);
            case WISDOM -> wisdomRepository.findByUserId(userId)
                    .map(w -> w.getScore()).orElse(0);
            case CHARISMA -> charismaRepository.findByUserId(userId)
                    .map(c -> c.getScore()).orElse(0);
        };
    }

    private List<UUID> getParticipantIds(UUID userId) {
        List<UUID> friendIds = friendshipRepository.findFriendIdsByUserId(userId);
        List<UUID> participantIds = new ArrayList<>(friendIds);
        if (!participantIds.contains(userId)) {
            participantIds.add(userId);
        }
        return participantIds;
    }
}
