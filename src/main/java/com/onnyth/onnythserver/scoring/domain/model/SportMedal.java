package com.onnyth.onnythserver.scoring.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a user's sport medals and athletic achievements.
 * Child table of the Physique domain — a user can have multiple medals.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportMedal {

    private UUID id;
    private UUID userId;
    private String sport;
    private MedalType medalType;
    private String eventName;
    private Integer year;
    private String evidenceUrl;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
