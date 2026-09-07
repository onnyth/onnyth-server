package com.onnyth.onnythserver.achievement.adapter.in.rest.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AchievementUnlockResponse(
        List<AchievementResponse> unlockedAchievements) {
}
