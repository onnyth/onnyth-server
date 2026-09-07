package com.onnyth.onnythserver.friendship.adapter.in.rest.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FriendResponse(
        UUID userId,
        String username,
        String fullName,
        String profilePic,
        String rankTier,
        long totalScore,
        Instant friendSince) {
}
