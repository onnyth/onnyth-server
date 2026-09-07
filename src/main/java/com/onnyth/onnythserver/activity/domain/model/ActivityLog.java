package com.onnyth.onnythserver.activity.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Records a single activity log entry — a user completing an activity.
 * Tracks the XP earned and is used for cooldown validation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    private UUID id;
    private UUID userId;
    private UUID activityTypeId;
    private Integer xpEarned;

    @Builder.Default
    private Instant loggedAt = Instant.now();
}
