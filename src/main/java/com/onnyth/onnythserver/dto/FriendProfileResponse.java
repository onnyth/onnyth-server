package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.ranking.adapter.in.rest.dto.RankProgressResponse;
import lombok.Builder;

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
        StatComparisonResponse comparison) {
}
