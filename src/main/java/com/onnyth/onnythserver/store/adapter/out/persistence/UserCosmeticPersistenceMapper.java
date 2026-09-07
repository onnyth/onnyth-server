package com.onnyth.onnythserver.store.adapter.out.persistence;

import com.onnyth.onnythserver.store.domain.model.UserCosmetic;

public final class UserCosmeticPersistenceMapper {

    private UserCosmeticPersistenceMapper() {
    }

    public static UserCosmetic toDomain(UserCosmeticEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserCosmetic.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .cosmeticItemId(entity.getCosmeticItemId())
                .purchasedAt(entity.getPurchasedAt())
                .isEquipped(entity.getIsEquipped())
                .build();
    }

    public static UserCosmeticEntity toEntity(UserCosmetic domain) {
        if (domain == null) {
            return null;
        }
        return UserCosmeticEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .cosmeticItemId(domain.getCosmeticItemId())
                .purchasedAt(domain.getPurchasedAt())
                .isEquipped(domain.getIsEquipped())
                .build();
    }
}
