package com.onnyth.onnythserver.friendship.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a bidirectional friendship between two users.
 * Each friendship creates 2 rows: A→B and B→A for efficient querying.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    private UUID id;
    private UUID userId;
    private UUID friendId;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
