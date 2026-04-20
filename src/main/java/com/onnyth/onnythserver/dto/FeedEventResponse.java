package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.FeedEventType;
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
