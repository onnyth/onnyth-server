package com.onnyth.onnythserver.lifestats.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a user's physical fitness data for the Physique domain.
 * Body composition is self-reported for MVP; workout integration comes later.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPhysique {

    private UUID id;
    private UUID userId;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private BigDecimal bodyFatPct;
    private FitnessLevel fitnessLevel;
    private String workoutSource;

    @Builder.Default
    private Integer weeklyWorkouts = 0;

    @Builder.Default
    private Integer score = 0;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
