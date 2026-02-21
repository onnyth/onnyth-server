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
     * Factory method to create a ProfileCardResponse from a User entity and score.
     *
     * @param user       the user entity
     * @param totalScore the user's total life score
     * @return a fully populated ProfileCardResponse
     */
    public static ProfileCardResponse fromUser(User user, long totalScore) {
        RankTier tier = RankTier.fromScore(totalScore);
        return ProfileCardResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .profilePic(user.getProfilePic())
                .totalScore(totalScore)
                .rankTier(tier.getDisplayName())
                .rankBadgeUrl(tier.getBadgeEmoji())
                .build();
    }
}
