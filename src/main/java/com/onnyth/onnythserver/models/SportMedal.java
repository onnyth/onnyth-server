package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a user's sport medals and athletic achievements.
 * Child table of the Physique domain — a user can have multiple medals.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sport_medals")
public class SportMedal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "sport", nullable = false, length = 50)
    private String sport;

    @Enumerated(EnumType.STRING)
    @Column(name = "medal_type", nullable = false, length = 20)
    private MedalType medalType;

    @Column(name = "event_name", length = 200)
    private String eventName;

    @Column(name = "year")
    private Integer year;

    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
