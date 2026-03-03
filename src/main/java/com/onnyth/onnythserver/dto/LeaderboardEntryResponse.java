package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import lombok.Builder;

import java.util.UUID;

/**
 * Response DTO for a single entry in the leaderboard.
 */
@Builder
public record LeaderboardEntryResponse(
        int rank,
        UUID userId,
        String username,
        String fullName,
        String profilePic,
        long totalScore,
        String rankTier,
        String rankBadge) {

    /**
     * Factory method to create a LeaderboardEntryResponse from a User entity.
     */
    public static LeaderboardEntryResponse fromUser(User user, int rank) {
        RankTier tier = user.getRankTier();
        return LeaderboardEntryResponse.builder()
                .rank(rank)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .profilePic(user.getProfilePic())
                .totalScore(user.getTotalScore())
                .rankTier(tier != null ? tier.getDisplayName() : "Bronze")
                .rankBadge(tier != null ? tier.getBadgeEmoji() : "🥉")
                .build();
    }
}
