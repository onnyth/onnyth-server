package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.ActivityType;
import com.onnyth.onnythserver.models.StatDomain;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ActivityTypeResponse(
        UUID id,
        String name,
        String description,
        String icon,
        StatDomain category,
        Integer xpReward,
        String frequency,
        Integer cooldownHours
) {
    public static ActivityTypeResponse fromEntity(ActivityType activityType) {
        return ActivityTypeResponse.builder()
                .id(activityType.getId())
                .name(activityType.getName())
                .description(activityType.getDescription())
                .icon(activityType.getIcon())
                .category(activityType.getCategory())
                .xpReward(activityType.getXpReward())
                .frequency(activityType.getFrequency().name())
                .cooldownHours(activityType.getCooldownHours())
                .build();
    }
}
