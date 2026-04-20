package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserLeaderboardPositionResponse(
        int position,
        int totalParticipants,
        long score,
        long pointsToNextPosition,
        String userAheadUsername,
        UUID userAheadId) {
}
