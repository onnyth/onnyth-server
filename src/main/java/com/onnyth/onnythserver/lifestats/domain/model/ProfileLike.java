package com.onnyth.onnythserver.lifestats.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a like on a user's profile/ID card.
 * Composite PK (liker_id, liked_id) prevents duplicate likes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileLike {

    private ProfileLikeId id;
    private UUID likerId;
    private UUID likedId;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
