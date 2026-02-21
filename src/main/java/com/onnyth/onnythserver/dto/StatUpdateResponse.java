package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.StatCategory;
import lombok.Builder;

/**
 * Response DTO for a stat update operation.
 * Includes previous value, new value, and score impact.
 */
@Builder
public record StatUpdateResponse(
        StatCategory category,
        String displayName,
        int previousValue,
        int newValue,
        long totalScore,
        long scoreChange) {
}
