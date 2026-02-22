package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for the RPG-style profile card view.
 * Contains essential user info plus gamification data (score + rank).
 */
@Builder
public record ProfileCardResponse(
        UUID userId,
        String username,
        String fullName,
        String profilePic,
        long totalScore,
        String rankTier,
        String rankBadgeUrl) {
    /**
     * Factory method to create a ProfileCardResponse from a User entity.
     * Reads rank tier directly from the User entity (persisted).
     *
     * @param user the user entity
     * @return a fully populated ProfileCardResponse
     */
    public static ProfileCardResponse fromUser(User user) {
        RankTier tier = user.getRankTier();
        return ProfileCardResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .profilePic(user.getProfilePic())
                .totalScore(user.getTotalScore())
                .rankTier(tier.getDisplayName())
                .rankBadgeUrl(tier.getBadgeEmoji())
                .build();
    }
}
