package com.onnyth.onnythserver.models;

import lombok.Getter;

/**
 * The 5 life stat categories that make up a user's life score.
 * Each category has a displayable name, valid value range, and a weight for
 * score calculation.
 */
@Getter
public enum StatCategory {

    CAREER("Career", 1, 100, 1.2),
    WEALTH("Wealth", 1, 100, 1.0),
    FITNESS("Fitness", 1, 100, 1.1),
    EDUCATION("Education", 1, 100, 1.3),
    SOCIAL_INFLUENCE("Social Influence", 1, 100, 0.9);

    private final String displayName;
    private final int minValue;
    private final int maxValue;
    private final double weight;

    StatCategory(String displayName, int minValue, int maxValue, double weight) {
        this.displayName = displayName;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.weight = weight;
    }

    /**
     * Checks if the given value is within the valid range for this category.
     */
    public boolean isValidValue(int value) {
        return value >= minValue && value <= maxValue;
    }
}
