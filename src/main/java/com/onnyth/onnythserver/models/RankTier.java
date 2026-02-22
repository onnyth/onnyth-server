package com.onnyth.onnythserver.models;

import lombok.Getter;

/**
 * RPG-style rank tiers derived from a user's total life score.
 * Each tier has a minimum score threshold, display name, and badge emoji.
 */
@Getter
public enum RankTier {

    BRONZE(0, "Bronze", "🥉"),
    SILVER(100, "Silver", "🥈"),
    GOLD(250, "Gold", "🥇"),
    PLATINUM(500, "Platinum", "💎"),
    ELITE(1000, "Elite", "👑");

    private final long minScore;
    private final String displayName;
    private final String badgeEmoji;

    RankTier(long minScore, String displayName, String badgeEmoji) {
        this.minScore = minScore;
        this.displayName = displayName;
        this.badgeEmoji = badgeEmoji;
    }

    /**
     * Determines the rank tier for the given score.
     * Returns the highest tier whose minimum score is ≤ the given score.
     *
     * @param totalScore the user's total life score
     * @return the corresponding RankTier
     */
    public static RankTier fromScore(long totalScore) {
        RankTier[] tiers = values();
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (totalScore >= tiers[i].minScore) {
                return tiers[i];
            }
        }
        return BRONZE;
    }

    /**
     * Returns the next tier above this one, or null if already at ELITE.
     *
     * @return the next RankTier, or null
     */
    public RankTier nextTier() {
        RankTier[] tiers = values();
        int nextOrdinal = this.ordinal() + 1;
        return nextOrdinal < tiers.length ? tiers[nextOrdinal] : null;
    }
}
