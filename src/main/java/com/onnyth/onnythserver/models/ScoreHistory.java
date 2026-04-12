package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit trail for score changes across any of the 5 stat domains or the total score.
 * Replaces the old life_stat_history table.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "score_history")
public class ScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain", nullable = false, length = 20)
    private StatDomain domain;

    @Column(name = "old_score", nullable = false)
    private Integer oldScore;

    @Column(name = "new_score", nullable = false)
    private Integer newScore;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant changedAt = Instant.now();
}
