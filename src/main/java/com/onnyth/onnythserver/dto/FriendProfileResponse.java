package com.onnyth.onnythserver.dto;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record FriendProfileResponse(
        UUID userId,
        String username,
        String fullName,
        String profilePic,
        String rankTier,
        long totalScore,
        RankProgressResponse rankProgress,
        List<LifeStatResponse> stats,
        StatComparisonResponse comparison) {
}
