package com.onnyth.onnythserver.quest.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Records that a user has completed a specific quest.
 * Unique constraint on (userId, questId) prevents double completion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestCompletion {

    private UUID id;
    private UUID userId;
    private UUID questId;

    @Builder.Default
    private Instant completedAt = Instant.now();
}
