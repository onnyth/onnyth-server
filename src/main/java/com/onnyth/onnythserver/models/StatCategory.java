package com.onnyth.onnythserver.models;

import lombok.Getter;

/**
 * The 5 life stat categories that make up a user's life score.
 * Each category has a displayable name and a valid value range.
 */
@Getter
public enum StatCategory {

    CAREER("Career", 1, 100),
    WEALTH("Wealth", 1, 100),
    FITNESS("Fitness", 1, 100),
    EDUCATION("Education", 1, 100),
    SOCIAL_INFLUENCE("Social Influence", 1, 100);

    private final String displayName;
    private final int minValue;
    private final int maxValue;

    StatCategory(String displayName, int minValue, int maxValue) {
        this.displayName = displayName;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Checks if the given value is within the valid range for this category.
     */
    public boolean isValidValue(int value) {
        return value >= minValue && value <= maxValue;
    }
}
