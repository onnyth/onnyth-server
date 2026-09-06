package com.onnyth.onnythserver.profile.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a user's upvote or downvote on another user's profile.
 * Each (voter, target) pair is unique — a user can only vote once per profile.
 * Changing the vote updates the existing record (upsert via the use case layer).
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProfileVote {

    private UUID voterId;
    private UUID targetId;

    /** true = upvote, false = downvote */
    private Boolean isUpvote;

    @Builder.Default
    private Instant votedAt = Instant.now();
}
