package com.onnyth.onnythserver.models;

import lombok.Getter;

/**
 * The 5 real-world stat domains that make up a user's total life score.
 * Each domain has a display name and weight for total score calculation.
 */
@Getter
public enum StatDomain {

    OCCUPATION("Occupation", 1.2),
    WEALTH("Wealth", 1.0),
    PHYSIQUE("Physique", 1.1),
    WISDOM("Wisdom", 1.3),
    CHARISMA("Charisma", 0.9);

    private final String displayName;
    private final double weight;

    StatDomain(String displayName, double weight) {
        this.displayName = displayName;
        this.weight = weight;
    }
}
