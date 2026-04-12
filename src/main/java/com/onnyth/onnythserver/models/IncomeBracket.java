package com.onnyth.onnythserver.models;

import lombok.Getter;

/**
 * Income bracket tiers for the Wealth domain.
 * Brackets are used instead of exact values for privacy and anti-gaming.
 * Each bracket has a score contribution for the wealth domain score calculation.
 */
@Getter
public enum IncomeBracket {

    UNDER_25K("Under $25K", 5),
    K25_50K("$25K - $50K", 10),
    K50_75K("$50K - $75K", 15),
    K75_100K("$75K - $100K", 20),
    K100_150K("$100K - $150K", 25),
    K150_250K("$150K - $250K", 30),
    K250_500K("$250K - $500K", 35),
    OVER_500K("Over $500K", 40);

    private final String displayName;
    private final int scoreContribution;

    IncomeBracket(String displayName, int scoreContribution) {
        this.displayName = displayName;
        this.scoreContribution = scoreContribution;
    }

    /**
     * Maps the DB string value back to the enum.
     * DB stores: UNDER_25K, 25K_50K, 50K_75K, etc.
     */
    public String toDbValue() {
        return switch (this) {
            case UNDER_25K -> "UNDER_25K";
            case K25_50K -> "25K_50K";
            case K50_75K -> "50K_75K";
            case K75_100K -> "75K_100K";
            case K100_150K -> "100K_150K";
            case K150_250K -> "150K_250K";
            case K250_500K -> "250K_500K";
            case OVER_500K -> "OVER_500K";
        };
    }

    public static IncomeBracket fromDbValue(String dbValue) {
        if (dbValue == null) return null;
        return switch (dbValue) {
            case "UNDER_25K" -> UNDER_25K;
            case "25K_50K" -> K25_50K;
            case "50K_75K" -> K50_75K;
            case "75K_100K" -> K75_100K;
            case "100K_150K" -> K100_150K;
            case "150K_250K" -> K150_250K;
            case "250K_500K" -> K250_500K;
            case "OVER_500K" -> OVER_500K;
            default -> throw new IllegalArgumentException("Unknown income bracket: " + dbValue);
        };
    }
}
