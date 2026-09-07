package com.onnyth.onnythserver.store.adapter.out.persistence;

import com.onnyth.onnythserver.store.domain.model.CosmeticItem;

public final class CosmeticItemPersistenceMapper {

    private CosmeticItemPersistenceMapper() {
    }

    public static CosmeticItem toDomain(CosmeticItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return CosmeticItem.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .previewUrl(entity.getPreviewUrl())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .rarity(entity.getRarity())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static CosmeticItemEntity toEntity(CosmeticItem domain) {
        if (domain == null) {
            return null;
        }
        return CosmeticItemEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .previewUrl(domain.getPreviewUrl())
                .category(domain.getCategory())
                .price(domain.getPrice())
                .rarity(domain.getRarity())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
