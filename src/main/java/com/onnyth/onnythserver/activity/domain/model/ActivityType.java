package com.onnyth.onnythserver.activity.domain.model;

import com.onnyth.onnythserver.models.StatDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Defines an activity that users can log to earn XP.
 * Each activity type belongs to a stat domain and has an XP reward,
 * frequency, and cooldown period.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityType {

    private UUID id;
    private String name;
    private String description;
    private String icon;
    private StatDomain category;
    private Integer xpReward;

    @Builder.Default
    private ActivityFrequency frequency = ActivityFrequency.DAILY;

    @Builder.Default
    private Integer cooldownHours = 24;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
