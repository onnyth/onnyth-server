package com.onnyth.onnythserver.quest.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class QuestCompletionEntity {

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
