package com.onnyth.onnythserver.scoring.adapter.out.persistence;

import com.onnyth.onnythserver.shared.domain.model.StatDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "score_history")
public class ScoreHistoryEntity {

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
