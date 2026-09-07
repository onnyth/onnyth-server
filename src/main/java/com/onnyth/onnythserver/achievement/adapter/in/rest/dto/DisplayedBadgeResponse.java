package com.onnyth.onnythserver.achievement.adapter.in.rest.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DisplayedBadgeResponse(
        UUID id,
        String name,
        String icon,
        UUID achievementId) {
}
