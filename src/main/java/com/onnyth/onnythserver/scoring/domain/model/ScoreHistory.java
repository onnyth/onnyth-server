package com.onnyth.onnythserver.scoring.domain.model;

import com.onnyth.onnythserver.shared.domain.model.StatDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit trail for score changes across any of the 5 stat domains or the total score.
 * Replaces the old life_stat_history table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreHistory {

    private UUID id;
    private UUID userId;
    private StatDomain domain;
    private Integer oldScore;
    private Integer newScore;
    private String reason;

    @Builder.Default
    private Instant changedAt = Instant.now();
}
