package com.onnyth.onnythserver.streak.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Tracks a user's daily activity streak.
 * One record per user — updated each time they log an activity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreak {

    private UUID id;
    private UUID userId;

    @Builder.Default
    private Integer currentStreak = 0;

    @Builder.Default
    private Integer longestStreak = 0;

    private LocalDate lastActivityDate;
}
