package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.models.Achievement;
import com.onnyth.onnythserver.models.LifeStat;
import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.FriendshipRepository;
import com.onnyth.onnythserver.repository.LifeStatRepository;
import com.onnyth.onnythserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Computes 0–100 progress for each achievement based on its requirement type.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AchievementProgressCalculator {

    private final UserRepository userRepository;
    private final LifeStatRepository lifeStatRepository;
    private final FriendshipRepository friendshipRepository;

    /**
     * Calculate progress (0–100) for a given achievement for the specified user.
     */
    public int calculateProgress(UUID userId, Achievement achievement) {
        String reqType = achievement.getRequirementType();
        int threshold = achievement.getThreshold();

        return switch (reqType) {
            case String s when s.startsWith("STAT_VALUE_") -> {
                String categoryName = s.replace("STAT_VALUE_", "");
                yield calculateStatProgress(userId, categoryName, threshold);
            }
            case "ALL_STATS_MIN" -> calculateAllStatsMinProgress(userId, threshold);
            case "FRIEND_COUNT" -> calculateFriendCountProgress(userId, threshold);
            case "TOTAL_SCORE" -> calculateTotalScoreProgress(userId, threshold);
            case "RANK_TIER" -> calculateRankTierProgress(userId, threshold);
            case "PROFILE_COMPLETE" -> calculateProfileCompleteProgress(userId);
            case "ANY_STAT_INPUT" -> calculateAnyStatInputProgress(userId);
            case "UPDATE_STREAK" -> 0; // Placeholder for future streak tracking
            default -> {
                log.warn("Unknown requirement type: {}", reqType);
                yield 0;
            }
        };
    }

    private int calculateStatProgress(UUID userId, String categoryName, int threshold) {
        try {
            var category = com.onnyth.onnythserver.models.StatCategory.valueOf(categoryName);
            return lifeStatRepository.findByUserIdAndCategory(userId, category)
                    .map(stat -> Math.min(100, (stat.getValue() * 100) / threshold))
                    .orElse(0);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown stat category in requirement: {}", categoryName);
            return 0;
        }
    }

    private int calculateAllStatsMinProgress(UUID userId, int threshold) {
        List<LifeStat> stats = lifeStatRepository.findAllByUserId(userId);
        if (stats.isEmpty()) return 0;

        // All 5 categories must be at least threshold
        int categoriesRequired = 5;
        int categoriesMet = (int) stats.stream()
                .filter(s -> s.getValue() >= threshold)
                .count();

        return Math.min(100, (categoriesMet * 100) / categoriesRequired);
    }

    private int calculateFriendCountProgress(UUID userId, int threshold) {
        int friendCount = friendshipRepository.findFriendIdsByUserId(userId).size();
        return Math.min(100, (friendCount * 100) / threshold);
    }

    private int calculateTotalScoreProgress(UUID userId, int threshold) {
        return userRepository.findById(userId)
                .map(user -> Math.min(100, (int) ((user.getTotalScore() * 100) / threshold)))
                .orElse(0);
    }

    private int calculateRankTierProgress(UUID userId, int threshold) {
        return userRepository.findById(userId)
                .map(user -> {
                    int currentOrdinal = user.getRankTier().ordinal();
                    return Math.min(100, (currentOrdinal * 100) / threshold);
                })
                .orElse(0);
    }

    private int calculateProfileCompleteProgress(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> Boolean.TRUE.equals(user.getProfileComplete()) ? 100 : 0)
                .orElse(0);
    }

    private int calculateAnyStatInputProgress(UUID userId) {
        List<LifeStat> stats = lifeStatRepository.findAllByUserId(userId);
        return stats.isEmpty() ? 0 : 100;
    }
}
