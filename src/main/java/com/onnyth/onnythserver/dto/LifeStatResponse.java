package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.LifeStat;
import com.onnyth.onnythserver.models.StatCategory;
import lombok.Builder;

import java.time.Instant;

/**
 * Response DTO for a single life stat entry.
 */
@Builder
public record LifeStatResponse(
        StatCategory category,
        String displayName,
        int value,
        int pointsContributed,
        Instant lastUpdated,
        String metadata) {
    /**
     * Factory method to create a LifeStatResponse from a LifeStat entity.
     * Points contributed defaults to the stat value for now.
     */
    public static LifeStatResponse fromEntity(LifeStat entity) {
        return LifeStatResponse.builder()
                .category(entity.getCategory())
                .displayName(entity.getCategory().getDisplayName())
                .value(entity.getValue())
                .pointsContributed(entity.getValue()) // TODO: scoring algorithm in Sprint 2
                .lastUpdated(entity.getLastUpdated())
                .metadata(entity.getMetadata())
                .build();
    }
}
