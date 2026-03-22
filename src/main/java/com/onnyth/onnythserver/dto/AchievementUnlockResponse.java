package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AchievementUnlockResponse(
        List<AchievementResponse> unlockedAchievements) {
}
