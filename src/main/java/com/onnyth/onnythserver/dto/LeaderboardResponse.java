package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.util.List;

/**
 * Response DTO for the leaderboard endpoint.
 * Contains the ranked entries, the requesting user's own rank, and total user
 * count.
 */
@Builder
public record LeaderboardResponse(
        List<LeaderboardEntryResponse> entries,
        Integer userRank,
        long totalUsers) {
}
