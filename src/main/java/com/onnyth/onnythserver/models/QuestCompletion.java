package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Records that a user has completed a specific quest.
 * Unique constraint on (userId, questId) prevents double completion.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quest_completions", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "quest_id" }))
public class QuestCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "quest_id", nullable = false)
    private UUID questId;

    @Column(name = "completed_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant completedAt = Instant.now();
}
