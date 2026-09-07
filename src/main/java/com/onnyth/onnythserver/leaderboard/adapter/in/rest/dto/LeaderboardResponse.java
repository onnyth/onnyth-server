package com.onnyth.onnythserver.leaderboard.adapter.in.rest.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record LeaderboardResponse(
        List<LeaderboardEntryResponse> entries,
        int totalFriends,
        int currentUserPosition,
        long currentUserScore) {
}
