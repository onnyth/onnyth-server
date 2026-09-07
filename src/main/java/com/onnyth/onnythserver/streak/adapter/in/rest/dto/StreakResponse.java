package com.onnyth.onnythserver.streak.adapter.in.rest.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record StreakResponse(
        Integer currentStreak,
        Integer longestStreak,
        LocalDate lastActivityDate,
        Boolean isActive,
        Integer nextMilestone,
        Integer nextMilestoneReward
) {
}
