package com.onnyth.onnythserver.dto;

import lombok.Builder;

@Builder
public record LevelProgressResponse(
        Integer currentLevel,
        String title,
        Long currentXP,
        Long xpForNextLevel,
        Double progressPercent
) {
}
