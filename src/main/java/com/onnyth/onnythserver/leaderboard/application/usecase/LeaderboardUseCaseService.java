package com.onnyth.onnythserver.leaderboard.application.usecase;

import com.onnyth.onnythserver.leaderboard.adapter.in.rest.dto.CategoryLeaderboardEntryResponse;
import com.onnyth.onnythserver.leaderboard.adapter.in.rest.dto.LeaderboardEntryResponse;
import com.onnyth.onnythserver.leaderboard.adapter.in.rest.dto.LeaderboardResponse;
import com.onnyth.onnythserver.leaderboard.adapter.in.rest.dto.UserLeaderboardPositionResponse;
import com.onnyth.onnythserver.lifestats.application.port.UserCharismaRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserOccupationRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserPhysiqueRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserWealthRepository;
import com.onnyth.onnythserver.lifestats.application.port.UserWisdomRepository;
import com.onnyth.onnythserver.shared.domain.model.StatDomain;
import com.onnyth.onnythserver.friendship.application.port.FriendshipRepository;
import com.onnyth.onnythserver.user.application.port.UserRepository;
import com.onnyth.onnythserver.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class LeaderboardUseCaseService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserOccupationRepository occupationRepository;
    private final UserWealthRepository wealthRepository;
    private final UserPhysiqueRepository physiqueRepository;
    private final UserWisdomRepository wisdomRepository;
    private final UserCharismaRepository charismaRepository;
    private final LeaderboardSnapshotUseCaseService snapshotService;

    @Transactional(readOnly = true)
    public LeaderboardResponse getFriendsLeaderboard(UUID userId, Pageable pageable) {
        List<UUID> participantIds = getParticipantIds(userId);
        List<User> allParticipants = new ArrayList<>(userRepository.findAllById(participantIds));

        allParticipants.sort(Comparator.comparingLong(User::getTotalScore).reversed());

        int currentUserPosition = 0;
        long currentUserScore = 0;
        for (int i = 0; i < allParticipants.size(); i++) {
            if (allParticipants.get(i).getId().equals(userId)) {
                currentUserPosition = i + 1;
                currentUserScore = allParticipants.get(i).getTotalScore();
                break;
            }
        }

        Map<UUID, Integer> positionChanges = snapshotService.getPositionChanges(userId, participantIds);
        Set<UUID> snapshotUserIds = snapshotService.getSnapshotUserIds(userId);

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
                .totalFriends(allParticipants.size() - 1)
                .currentUserPosition(currentUserPosition)
                .currentUserScore(currentUserScore)
                .build();
    }

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

        String userAheadUsername = null;
        UUID userAheadId = null;
        long pointsToNext = 0;

        if (position > 1) {
            User ahead = allParticipants.get(position - 2);
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

    @Transactional(readOnly = true)
    public Page<CategoryLeaderboardEntryResponse> getLeaderboardByCategory(
            UUID userId, StatDomain domain, Pageable pageable) {
        List<UUID> participantIds = getParticipantIds(userId);
        List<User> allParticipants = userRepository.findAllById(participantIds);
        Map<UUID, User> usersById = allParticipants.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<UUID, Integer> scoreMap = new HashMap<>();
        for (UUID pid : participantIds) {
            scoreMap.put(pid, getDomainScore(pid, domain));
        }

        List<Map.Entry<UUID, Integer>> ranked = new ArrayList<>(scoreMap.entrySet());
        ranked.sort((left, right) -> Integer.compare(right.getValue(), left.getValue()));

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), ranked.size());
        List<Map.Entry<UUID, Integer>> pageContent = start < ranked.size()
                ? ranked.subList(start, end)
                : List.of();

        List<CategoryLeaderboardEntryResponse> entries = new ArrayList<>();
        for (int i = 0; i < pageContent.size(); i++) {
            Map.Entry<UUID, Integer> entry = pageContent.get(i);
            User user = usersById.get(entry.getKey());
            if (user == null) {
                continue;
            }

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

    private List<UUID> getParticipantIds(UUID userId) {
        List<UUID> friendIds = friendshipRepository.findFriendIdsByUserId(userId);
        List<UUID> participantIds = new ArrayList<>(friendIds);
        if (!participantIds.contains(userId)) {
            participantIds.add(userId);
        }
        return participantIds;
    }
}
