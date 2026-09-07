package com.onnyth.onnythserver.achievement.application.usecase;

import com.onnyth.onnythserver.achievement.adapter.in.rest.dto.AchievementResponse;
import com.onnyth.onnythserver.achievement.adapter.in.rest.dto.AchievementStatsResponse;
import com.onnyth.onnythserver.achievement.adapter.in.rest.dto.DisplayedBadgeResponse;
import com.onnyth.onnythserver.achievement.application.AchievementProgressCalculator;
import com.onnyth.onnythserver.achievement.application.exception.BadgeNotFoundException;
import com.onnyth.onnythserver.achievement.application.exception.BadgeNotUnlockedException;
import com.onnyth.onnythserver.achievement.application.port.AchievementRepository;
import com.onnyth.onnythserver.achievement.application.port.UserAchievementRepository;
import com.onnyth.onnythserver.achievement.domain.model.Achievement;
import com.onnyth.onnythserver.achievement.domain.model.AchievementCategory;
import com.onnyth.onnythserver.achievement.domain.model.UserAchievement;
import com.onnyth.onnythserver.friendship.application.port.FriendshipRepository;
import com.onnyth.onnythserver.user.application.exception.UserNotFoundException;
import com.onnyth.onnythserver.user.application.port.UserRepository;
import com.onnyth.onnythserver.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementUseCaseService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final AchievementProgressCalculator progressCalculator;

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAllAchievements(UUID userId) {
        List<Achievement> achievements = achievementRepository.findAllByIsActiveTrue();
        Map<UUID, UserAchievement> unlockMap = getUserUnlockMap(userId);
        return achievements.stream()
                .map(a -> toResponse(a, unlockMap, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getUnlockedAchievements(UUID userId) {
        List<UserAchievement> userAchievements = userAchievementRepository.findAllByUserId(userId);
        Set<UUID> unlockedIds = userAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());
        Map<UUID, UserAchievement> unlockMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, Function.identity()));

        return achievementRepository.findAllById(unlockedIds).stream()
                .map(a -> toResponse(a, unlockMap, userId))
                .sorted(Comparator.comparing(AchievementResponse::unlockedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAchievementsByCategory(UUID userId, AchievementCategory category) {
        List<Achievement> achievements = achievementRepository.findAllByCategory(category);
        Map<UUID, UserAchievement> unlockMap = getUserUnlockMap(userId);
        return achievements.stream()
                .map(a -> toResponse(a, unlockMap, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public AchievementStatsResponse getAchievementStats(UUID userId) {
        List<Achievement> allActive = achievementRepository.findAllByIsActiveTrue();
        List<UserAchievement> unlocked = userAchievementRepository.findAllByUserId(userId);
        Set<UUID> unlockedIds = unlocked.stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        int totalPoints = allActive.stream().mapToInt(Achievement::getPoints).sum();
        int earnedPoints = allActive.stream()
                .filter(a -> unlockedIds.contains(a.getId()))
                .mapToInt(Achievement::getPoints).sum();

        return AchievementStatsResponse.builder()
                .totalAchievements(allActive.size())
                .unlockedCount(unlocked.size())
                .totalPoints(totalPoints)
                .earnedPoints(earnedPoints)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DisplayedBadgeResponse> getDisplayedBadges(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        List<UUID> displayedIds = user.getDisplayedAchievements();
        if (displayedIds == null || displayedIds.isEmpty()) {
            return List.of();
        }

        List<Achievement> achievements = achievementRepository.findAllById(displayedIds);
        return achievements.stream()
                .map(a -> DisplayedBadgeResponse.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .icon(a.getIcon())
                        .achievementId(a.getId())
                        .build())
                .toList();
    }

    @Transactional
    public List<DisplayedBadgeResponse> updateDisplayedBadges(UUID userId, List<UUID> achievementIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        List<UUID> uniqueIds = achievementIds.stream().distinct().toList();

        if (uniqueIds.size() > 3) {
            throw new IllegalArgumentException("You can display a maximum of 3 badges");
        }

        for (UUID achId : uniqueIds) {
            Achievement achievement = achievementRepository.findById(achId)
                    .orElseThrow(() -> new BadgeNotFoundException("Achievement not found: " + achId));
            if (!userAchievementRepository.existsByUserIdAndAchievementId(userId, achId)) {
                throw new BadgeNotUnlockedException("Achievement not unlocked: " + achievement.getName());
            }
        }

        user.setDisplayedAchievements(new ArrayList<>(uniqueIds));
        userRepository.save(user);

        return getDisplayedBadges(userId);
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getFriendAchievements(UUID userId, UUID friendId) {
        validateFriendship(userId, friendId);
        return getUnlockedAchievements(friendId);
    }

    @Transactional(readOnly = true)
    public AchievementStatsResponse getFriendAchievementStats(UUID userId, UUID friendId) {
        validateFriendship(userId, friendId);
        return getAchievementStats(friendId);
    }

    private Map<UUID, UserAchievement> getUserUnlockMap(UUID userId) {
        return userAchievementRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, Function.identity()));
    }

    private AchievementResponse toResponse(Achievement achievement, Map<UUID, UserAchievement> unlockMap, UUID userId) {
        UserAchievement userAchievement = unlockMap.get(achievement.getId());
        boolean isUnlocked = userAchievement != null;
        int progress = isUnlocked ? 100 : progressCalculator.calculateProgress(userId, achievement);

        return AchievementResponse.builder()
                .id(achievement.getId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .icon(achievement.getIcon())
                .category(achievement.getCategory().getDisplayName())
                .points(achievement.getPoints())
                .isUnlocked(isUnlocked)
                .progress(progress)
                .unlockedAt(userAchievement != null ? userAchievement.getUnlockedAt() : null)
                .build();
    }

    private void validateFriendship(UUID userId, UUID friendId) {
        if (!friendshipRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new IllegalArgumentException("User " + friendId + " is not your friend");
        }
    }
}
