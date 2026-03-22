package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AchievementResponse(
        UUID id,
        String name,
        String description,
        String icon,
        String category,
        int points,
        boolean isUnlocked,
        int progress,
        Instant unlockedAt) {
}
