package com.onnyth.onnythserver.service;

import com.onnyth.onnythserver.models.Achievement;
import com.onnyth.onnythserver.models.StatDomain;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Computes 0–100 progress for each achievement based on its requirement type.
 * Updated to use the new domain-specific stat repositories instead of LifeStat.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AchievementProgressCalculator {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserOccupationRepository occupationRepository;
    private final UserWealthRepository wealthRepository;
    private final UserPhysiqueRepository physiqueRepository;
    private final UserWisdomRepository wisdomRepository;
    private final UserCharismaRepository charismaRepository;

    /**
     * Calculate progress (0–100) for a given achievement for the specified user.
     */
    public int calculateProgress(UUID userId, Achievement achievement) {
        String reqType = achievement.getRequirementType();
        int threshold = achievement.getThreshold();

        return switch (reqType) {
            case String s when s.startsWith("STAT_VALUE_") -> {
                String domainName = s.replace("STAT_VALUE_", "");
                yield calculateDomainScoreProgress(userId, domainName, threshold);
            }
            case "ALL_STATS_MIN" -> calculateAllDomainsMinProgress(userId, threshold);
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

    private int calculateDomainScoreProgress(UUID userId, String domainName, int threshold) {
        try {
            StatDomain domain = StatDomain.valueOf(domainName);
            int score = getDomainScore(userId, domain);
            return Math.min(100, (score * 100) / Math.max(1, threshold));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown stat domain in requirement: {}", domainName);
            return 0;
        }
    }

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

    private int calculateAllDomainsMinProgress(UUID userId, int threshold) {
        int domainsMet = 0;
        int totalDomains = StatDomain.values().length; // 5

        for (StatDomain domain : StatDomain.values()) {
            if (getDomainScore(userId, domain) >= threshold) {
                domainsMet++;
            }
        }

        return Math.min(100, (domainsMet * 100) / totalDomains);
    }

    private int calculateFriendCountProgress(UUID userId, int threshold) {
        int friendCount = friendshipRepository.findFriendIdsByUserId(userId).size();
        return Math.min(100, (friendCount * 100) / Math.max(1, threshold));
    }

    private int calculateTotalScoreProgress(UUID userId, int threshold) {
        return userRepository.findById(userId)
                .map(user -> Math.min(100, (int) ((user.getTotalScore() * 100) / Math.max(1, threshold))))
                .orElse(0);
    }

    private int calculateRankTierProgress(UUID userId, int threshold) {
        return userRepository.findById(userId)
                .map(user -> {
                    int currentOrdinal = user.getRankTier().ordinal();
                    return Math.min(100, (currentOrdinal * 100) / Math.max(1, threshold));
                })
                .orElse(0);
    }

    private int calculateProfileCompleteProgress(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> Boolean.TRUE.equals(user.getProfileComplete()) ? 100 : 0)
                .orElse(0);
    }

    private int calculateAnyStatInputProgress(UUID userId) {
        // Check if user has any domain stat populated
        for (StatDomain domain : StatDomain.values()) {
            if (getDomainScore(userId, domain) > 0) {
                return 100;
            }
        }
        return 0;
    }
}
