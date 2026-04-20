package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ActivityLogResponse(
        UUID id,
        ActivityTypeResponse activityType,
        Integer xpEarned,
        Instant loggedAt,
        Long newTotalXP,
        Integer newLevel,
        String levelTitle,
        Boolean streakUpdated
) {
}
