package com.onnyth.onnythserver.models;

import lombok.Getter;

/**
 * Education levels for the Wisdom domain.
 * Ordered by academic attainment, each with a score contribution.
 */
@Getter
public enum EducationLevel {
    HIGH_SCHOOL("High School", 5),
    BOOTCAMP("Bootcamp", 10),
    SELF_TAUGHT("Self-Taught", 10),
    ASSOCIATE("Associate Degree", 12),
    CERTIFICATION("Professional Certification", 15),
    BACHELORS("Bachelor's Degree", 20),
    MASTERS("Master's Degree", 28),
    PHD("PhD / Doctorate", 35);

    private final String displayName;
    private final int scoreContribution;

    EducationLevel(String displayName, int scoreContribution) {
        this.displayName = displayName;
        this.scoreContribution = scoreContribution;
    }
}
