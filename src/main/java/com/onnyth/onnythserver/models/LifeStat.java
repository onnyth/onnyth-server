package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single life stat entry for a user.
 * Each user has at most one LifeStat per StatCategory (unique constraint on
 * user_id + category).
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "life_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "category" })
})
public class LifeStat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private StatCategory category;

    @Column(name = "value", nullable = false)
    private int value;

    @Column(name = "previous_value")
    private Integer previousValue;

    @ColumnDefault("now()")
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
