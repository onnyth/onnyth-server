package com.onnyth.onnythserver.lifestats.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregates a user's social presence metrics for the Charisma domain.
 * onnyth_profile_likes is a denormalized counter for fast reads (source of truth: profile_likes table).
 * Onnyth follower count is derived from the follows table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCharisma {

    private UUID id;
    private UUID userId;

    @Builder.Default
    private Integer onnythProfileLikes = 0;

    @Builder.Default
    private Integer score = 0;

    private Instant lastSocialSyncAt;
    private String relationshipStatus;
    private Integer socialCircleSize;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
