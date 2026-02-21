package com.onnyth.onnythserver.models;

import lombok.Getter;

/**
 * RPG-style rank tiers derived from a user's total life score.
 * Each tier has a minimum score threshold, display name, and badge emoji.
 */
@Getter
public enum RankTier {

    NOVICE(0, "Novice", "🟤"),
    APPRENTICE(100, "Apprentice", "🟢"),
    JOURNEYMAN(500, "Journeyman", "🔵"),
    EXPERT(1500, "Expert", "🟣"),
    MASTER(5000, "Master", "🟠"),
    GRANDMASTER(15000, "Grandmaster", "🔴"),
    LEGEND(50000, "Legend", "⭐");

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
        // Walk backwards from the highest tier
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (totalScore >= tiers[i].minScore) {
                return tiers[i];
            }
        }
        return NOVICE;
    }
}
