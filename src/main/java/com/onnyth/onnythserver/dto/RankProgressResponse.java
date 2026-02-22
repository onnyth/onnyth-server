package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.RankTier;
import lombok.Builder;

/**
 * DTO for rank progress information.
 * Shows current tier, score, next tier, and progress towards it.
 */
@Builder
public record RankProgressResponse(
        String currentTier,
        String currentBadge,
        long currentScore,
        String nextTier,
        String nextBadge,
        long pointsToNextTier,
        double progressPercent) {
    /**
     * Factory method to create a RankProgressResponse from score and tier.
     */
    public static RankProgressResponse fromScoreAndTier(long totalScore, RankTier tier) {
        RankTier next = tier.nextTier();

        long pointsToNext;
        double progress;
        String nextTierName;
        String nextBadge;

        if (next != null) {
            long tierRange = next.getMinScore() - tier.getMinScore();
            long pointsEarned = totalScore - tier.getMinScore();
            pointsToNext = next.getMinScore() - totalScore;
            progress = tierRange > 0 ? Math.min(100.0, (double) pointsEarned / tierRange * 100.0) : 100.0;
            nextTierName = next.getDisplayName();
            nextBadge = next.getBadgeEmoji();
        } else {
            // Already at max tier
            pointsToNext = 0;
            progress = 100.0;
            nextTierName = null;
            nextBadge = null;
        }

        return RankProgressResponse.builder()
                .currentTier(tier.getDisplayName())
                .currentBadge(tier.getBadgeEmoji())
                .currentScore(totalScore)
                .nextTier(nextTierName)
                .nextBadge(nextBadge)
                .pointsToNextTier(pointsToNext)
                .progressPercent(Math.round(progress * 10.0) / 10.0)
                .build();
    }
}
