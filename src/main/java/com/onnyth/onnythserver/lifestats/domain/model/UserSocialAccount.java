package com.onnyth.onnythserver.lifestats.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Links a user to an external social media platform for the Charisma domain.
 * Extensible design: adding a new platform = new row, not a schema change.
 * Follower counts are self-reported for MVP, verified via API post-MVP.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSocialAccount {

    private UUID id;
    private UUID userId;
    private SocialPlatform platform;
    private String username;
    private String profileUrl;

    @Builder.Default
    private Integer followerCount = 0;

    @Builder.Default
    private Boolean isVerified = false;

    private Instant verifiedAt;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
