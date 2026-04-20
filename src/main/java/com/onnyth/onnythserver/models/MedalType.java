package com.onnyth.onnythserver.models;

import lombok.Getter;

/**
 * Medal types for sport achievements in the Physique domain.
 */
@Getter
public enum MedalType {
    GOLD("Gold", 10),
    SILVER("Silver", 7),
    BRONZE("Bronze", 5),
    CHAMPIONSHIP("Championship", 12),
    PARTICIPATION("Participation", 2);

    private final String displayName;
    private final int scoreContribution;

    MedalType(String displayName, int scoreContribution) {
        this.displayName = displayName;
        this.scoreContribution = scoreContribution;
    }
}
