package com.onnyth.onnythserver.achievement.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks per-user achievement unlock status.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievement {

    private UUID id;
    private UUID userId;
    private UUID achievementId;
    private Instant unlockedAt;
}
