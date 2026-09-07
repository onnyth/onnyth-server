package com.onnyth.onnythserver.feed.adapter.in.rest.dto;

import com.onnyth.onnythserver.feed.domain.model.FeedEventType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FeedEventResponse(
        UUID id,
        UUID userId,
        String username,
        String profilePic,
        FeedEventType eventType,
        String eventData,
        Instant createdAt
) {
}
