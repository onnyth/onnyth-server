package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record ActivityStatusResponse(
        List<ActivityTypeResponse> todayLogs,
        List<CooldownEntry> cooldowns
) {
    @Builder
    public record CooldownEntry(
            UUID activityTypeId,
            Instant availableAt
    ) {
    }
}
