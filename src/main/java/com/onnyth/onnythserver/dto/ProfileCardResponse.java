package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.RankTier;
import com.onnyth.onnythserver.models.User;
import com.onnyth.onnythserver.service.LevelService;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for the RPG-style profile card view.
 * Contains essential user info plus gamification data (score, rank, level, streak).
 */
@Builder
public record ProfileCardResponse(
        UUID userId,
        String username,
        String fullName,
        String profilePic,
        long totalScore,
        String rankTier,
        String rankBadgeUrl,
        Integer level,
        String levelTitle,
        int onnythCoins,
        Integer currentStreak) {
    /**
     * Factory method to create a ProfileCardResponse from a User entity.
     *
     * @param user          the user entity
     * @param currentStreak the user's current streak (0 if no streak data)
     * @return a fully populated ProfileCardResponse
     */
    public static ProfileCardResponse fromUser(User user, int currentStreak) {
        RankTier tier = user.getRankTier();
        return ProfileCardResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .profilePic(user.getProfilePic())
                .totalScore(user.getTotalScore())
                .rankTier(tier.getDisplayName())
                .rankBadgeUrl(tier.getBadgeEmoji())
                .level(user.getLevel())
                .levelTitle(LevelService.getTitle(user.getLevel()))
                .onnythCoins(user.getOnnythCoins() != null ? user.getOnnythCoins() : 0)
                .currentStreak(currentStreak)
                .build();
    }

    /**
     * Backward-compatible factory with no streak data.
     */
    public static ProfileCardResponse fromUser(User user) {
        return fromUser(user, 0);
    }
}

