package com.onnyth.onnythserver.models;

import lombok.Getter;

@Getter
public enum FitnessLevel {
    BEGINNER("Beginner", 5),
    INTERMEDIATE("Intermediate", 15),
    ADVANCED("Advanced", 25),
    ATHLETE("Athlete", 30),
    ELITE("Elite", 30);

    private final String displayName;
    private final int scoreContribution;

    FitnessLevel(String displayName, int scoreContribution) {
        this.displayName = displayName;
        this.scoreContribution = scoreContribution;
    }
}
