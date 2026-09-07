package com.onnyth.onnythserver.leaderboard.adapter.in.rest.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record LeaderboardEntryResponse(
        int position,
        UUID userId,
        String username,
        String fullName,
        String profilePic,
        long totalScore,
        String rankTier,
        boolean isCurrentUser,
        Integer positionChange,
        boolean isNew) {
}
