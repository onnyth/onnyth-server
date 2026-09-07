package com.onnyth.onnythserver.leaderboard.adapter.in.rest.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CategoryLeaderboardEntryResponse(
        int position,
        UUID userId,
        String username,
        String fullName,
        String profilePic,
        int categoryValue,
        String category,
        String rankTier,
        boolean isCurrentUser) {
}
