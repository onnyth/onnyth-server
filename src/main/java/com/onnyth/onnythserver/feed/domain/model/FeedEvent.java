package com.onnyth.onnythserver.feed.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A feed event that appears in the social activity feed.
 * Created when a user logs an activity, levels up, unlocks an achievement,
 * or reaches a streak milestone.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedEvent {

    private UUID id;
    private UUID userId;
    private FeedEventType eventType;
    private String eventData;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
