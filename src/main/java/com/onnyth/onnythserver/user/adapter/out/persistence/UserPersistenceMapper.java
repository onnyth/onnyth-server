package com.onnyth.onnythserver.user.adapter.out.persistence;

import com.onnyth.onnythserver.store.adapter.out.persistence.CosmeticItemPersistenceMapper;
import com.onnyth.onnythserver.user.domain.model.User;

public final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .fullName(entity.getFullName())
                .phone(entity.getPhone())
                .profileType(entity.getProfileType())
                .profilePic(entity.getProfilePic())
                .emailVerified(entity.getEmailVerified())
                .profileComplete(entity.getProfileComplete())
                .totalScore(entity.getTotalScore())
                .xp(entity.getXp())
                .level(entity.getLevel())
                .onnythCoins(entity.getOnnythCoins())
                .rankTier(entity.getRankTier())
                .displayedAchievements(entity.getDisplayedAchievements())
                .worldRank(entity.getWorldRank())
                .countryRank(entity.getCountryRank())
                .country(entity.getCountry())
                .voteScore(entity.getVoteScore())
                .activeBackgroundColor(entity.getActiveBackgroundColor())
                .activeFrameCosmetic(CosmeticItemPersistenceMapper.toDomain(entity.getActiveFrameCosmetic()))
                .activeBackgroundCosmetic(CosmeticItemPersistenceMapper.toDomain(entity.getActiveBackgroundCosmetic()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return UserEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .email(domain.getEmail())
                .fullName(domain.getFullName())
                .phone(domain.getPhone())
                .profileType(domain.getProfileType())
                .profilePic(domain.getProfilePic())
                .emailVerified(domain.getEmailVerified())
                .profileComplete(domain.getProfileComplete())
                .totalScore(domain.getTotalScore())
                .xp(domain.getXp())
                .level(domain.getLevel())
                .onnythCoins(domain.getOnnythCoins())
                .rankTier(domain.getRankTier())
                .displayedAchievements(domain.getDisplayedAchievements())
                .worldRank(domain.getWorldRank())
                .countryRank(domain.getCountryRank())
                .country(domain.getCountry())
                .voteScore(domain.getVoteScore())
                .activeBackgroundColor(domain.getActiveBackgroundColor())
                .activeFrameCosmetic(CosmeticItemPersistenceMapper.toEntity(domain.getActiveFrameCosmetic()))
                .activeBackgroundCosmetic(CosmeticItemPersistenceMapper.toEntity(domain.getActiveBackgroundCosmetic()))
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
