package com.onnyth.onnythserver.achievement.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AchievementCategory {
    STATS("Stats"),
    SOCIAL("Social"),
    STREAK("Streak"),
    MILESTONE("Milestone"),
    SPECIAL("Special");

    private final String displayName;
}
