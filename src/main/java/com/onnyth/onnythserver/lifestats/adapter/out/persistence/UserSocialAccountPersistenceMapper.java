package com.onnyth.onnythserver.lifestats.adapter.out.persistence;

import com.onnyth.onnythserver.lifestats.domain.model.UserSocialAccount;

public final class UserSocialAccountPersistenceMapper {

    private UserSocialAccountPersistenceMapper() {
    }

    public static UserSocialAccount toDomain(UserSocialAccountEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserSocialAccount.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .platform(entity.getPlatform())
                .username(entity.getUsername())
                .profileUrl(entity.getProfileUrl())
                .followerCount(entity.getFollowerCount())
                .isVerified(entity.getIsVerified())
                .verifiedAt(entity.getVerifiedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserSocialAccountEntity toEntity(UserSocialAccount domain) {
        if (domain == null) {
            return null;
        }
        return UserSocialAccountEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .platform(domain.getPlatform())
                .username(domain.getUsername())
                .profileUrl(domain.getProfileUrl())
                .followerCount(domain.getFollowerCount())
                .isVerified(domain.getIsVerified())
                .verifiedAt(domain.getVerifiedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
