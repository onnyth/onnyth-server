package com.onnyth.onnythserver.dto;

import lombok.Builder;

@Builder
public record AchievementStatsResponse(
        int totalAchievements,
        int unlockedCount,
        int totalPoints,
        int earnedPoints) {
}
