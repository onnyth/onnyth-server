package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StreakResponse(
        Integer currentStreak,
        Integer longestStreak,
        LocalDate lastActivityDate,
        Boolean isActive
) {
}
