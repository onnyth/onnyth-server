package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.dto.*;
import com.onnyth.onnythserver.exceptions.BadgeNotFoundException;
import com.onnyth.onnythserver.exceptions.BadgeNotUnlockedException;
import com.onnyth.onnythserver.exceptions.UserNotFoundException;
import com.onnyth.onnythserver.models.Achievement;
import com.onnyth.onnythserver.models.AchievementCategory;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.models.UserAchievement;
import com.onnyth.onnythserver.repository.AchievementRepository;
import com.onnyth.onnythserver.repository.FriendshipRepository;
import com.onnyth.onnythserver.repository.UserAchievementRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final AchievementProgressCalculator progressCalculator;

    // ─── Catalog ──────────────────────────────────────────────────────────────

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

    // ─── Stats ────────────────────────────────────────────────────────────────

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

    // ─── Badge Display ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DisplayedBadgeResponse> getDisplayedBadges(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        List<UUID> displayedIds = user.getDisplayedAchievements();
        if (displayedIds == null || displayedIds.isEmpty())
            return List.of();

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

        // Remove duplicates
        List<UUID> uniqueIds = achievementIds.stream().distinct().toList();

        if (uniqueIds.size() > 3) {
            throw new IllegalArgumentException("You can display a maximum of 3 badges");
        }

        // Validate all exist and are unlocked
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

    // ─── Friend Achievements ──────────────────────────────────────────────────

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

    // ─── Private helpers ──────────────────────────────────────────────────────

    private Map<UUID, UserAchievement> getUserUnlockMap(UUID userId) {
        return userAchievementRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, Function.identity()));
    }

    private AchievementResponse toResponse(Achievement a, Map<UUID, UserAchievement> unlockMap, UUID userId) {
        UserAchievement ua = unlockMap.get(a.getId());
        boolean isUnlocked = ua != null;
        int progress = isUnlocked ? 100 : progressCalculator.calculateProgress(userId, a);

        return AchievementResponse.builder()
                .id(a.getId())
                .name(a.getName())
                .description(a.getDescription())
                .icon(a.getIcon())
                .category(a.getCategory().getDisplayName())
                .points(a.getPoints())
                .isUnlocked(isUnlocked)
                .progress(progress)
                .unlockedAt(ua != null ? ua.getUnlockedAt() : null)
                .build();
    }

    private void validateFriendship(UUID userId, UUID friendId) {
        if (!friendshipRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new IllegalArgumentException("User " + friendId + " is not your friend");
        }
    }
}
