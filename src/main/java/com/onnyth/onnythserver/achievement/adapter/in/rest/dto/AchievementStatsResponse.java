package com.onnyth.onnythserver.achievement.adapter.in.rest.dto;

import lombok.Builder;

@Builder
public record AchievementStatsResponse(
        int totalAchievements,
        int unlockedCount,
        int totalPoints,
        int earnedPoints) {
}
