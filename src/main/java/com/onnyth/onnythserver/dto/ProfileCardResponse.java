package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.service.LevelService;
import lombok.Builder;

import java.util.UUID;

/**
 * Extended DTO for the OnnythID profile card.
 * Returned by GET /api/v1/profile/card (self) and GET /api/v1/profile/{userId}/card (viewer).
 *
 * Contains:
 * - Core identity (username, displayName, profilePic)
 * - Gamification (totalScore, level, rankTier, streak)
 * - Rankings (world rank, country rank)
 * - Domain scores (5 domains, each with raw score + label)
 * - Social (voteScore)
 * - Cosmetics (activeBackgroundColor, frame preview URL, background preview URL)
 */
@Builder
public record ProfileCardResponse(
        // ─── Identity ────────────────────────────────────────────────────────────
        UUID userId,
        String username,
        String fullName,
        String profilePic,
        String country,

        // ─── Gamification ────────────────────────────────────────────────────────
        long totalScore,
        String rankTier,
        String rankBadgeUrl,
        Integer level,
        String levelTitle,
        int onnythCoins,
        Integer currentStreak,

        // ─── Rankings ────────────────────────────────────────────────────────────
        Integer worldRank,
        Integer countryRank,

        // ─── Domain Scores ───────────────────────────────────────────────────────
        DomainScoreDto occupationScore,
        DomainScoreDto wealthScore,
        DomainScoreDto physiqueScore,
        DomainScoreDto wisdomScore,
        DomainScoreDto charismaScore,

        // ─── Social Votes ────────────────────────────────────────────────────────
        int voteScore,

        // ─── Cosmetics ───────────────────────────────────────────────────────────
        /** Hex solid background color (e.g. "#22162B"). Null if a cosmetic background is equipped. */
        String activeBackgroundColor,
        /** Preview URL for the equipped frame cosmetic. Null if none equipped. */
        String activeFramePreviewUrl,
        /** Preview URL for the equipped background cosmetic. Null if none equipped. */
        String activeBackgroundPreviewUrl
) {
    /**
     * Full factory: used by ProfileService when all domain scores are available.
     */
    public static ProfileCardResponse fromUser(
            User user,
            int currentStreak,
            DomainScoreDto occupationScore,
            DomainScoreDto wealthScore,
            DomainScoreDto physiqueScore,
            DomainScoreDto wisdomScore,
            DomainScoreDto charismaScore
    ) {
        RankTier tier = user.getRankTier();
        String frameUrl = user.getActiveFrameCosmetic() != null
                ? user.getActiveFrameCosmetic().getPreviewUrl() : null;
        String bgUrl = user.getActiveBackgroundCosmetic() != null
                ? user.getActiveBackgroundCosmetic().getPreviewUrl() : null;

        return ProfileCardResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .profilePic(user.getProfilePic())
                .country(user.getCountry())
                .totalScore(user.getTotalScore())
                .rankTier(tier.getDisplayName())
                .rankBadgeUrl(tier.getBadgeEmoji())
                .level(user.getLevel())
                .levelTitle(LevelService.getTitle(user.getLevel()))
                .onnythCoins(user.getOnnythCoins() != null ? user.getOnnythCoins() : 0)
                .currentStreak(currentStreak)
                .worldRank(user.getWorldRank())
                .countryRank(user.getCountryRank())
                .occupationScore(occupationScore)
                .wealthScore(wealthScore)
                .physiqueScore(physiqueScore)
                .wisdomScore(wisdomScore)
                .charismaScore(charismaScore)
                .voteScore(user.getVoteScore() != null ? user.getVoteScore() : 0)
                .activeBackgroundColor(user.getActiveBackgroundColor())
                .activeFramePreviewUrl(frameUrl)
                .activeBackgroundPreviewUrl(bgUrl)
                .build();
    }

    /**
     * Minimal factory — used when domain score data is not available (fallback).
     * All domain scores default to empty.
     */
    public static ProfileCardResponse fromUser(User user) {
        return fromUser(
                user, 0,
                DomainScoreDto.empty("OCCUPATION"),
                DomainScoreDto.empty("WEALTH"),
                DomainScoreDto.empty("PHYSIQUE"),
                DomainScoreDto.empty("WISDOM"),
                DomainScoreDto.empty("CHARISMA")
        );
    }

    /**
     * Minimal factory with streak but no domain scores.
     */
    public static ProfileCardResponse fromUser(User user, int currentStreak) {
        return fromUser(
                user, currentStreak,
                DomainScoreDto.empty("OCCUPATION"),
                DomainScoreDto.empty("WEALTH"),
                DomainScoreDto.empty("PHYSIQUE"),
                DomainScoreDto.empty("WISDOM"),
                DomainScoreDto.empty("CHARISMA")
        );
    }
}
