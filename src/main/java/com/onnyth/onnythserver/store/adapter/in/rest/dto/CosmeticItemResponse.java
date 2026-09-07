package com.onnyth.onnythserver.store.adapter.in.rest.dto;

import com.onnyth.onnythserver.store.domain.model.CosmeticCategory;
import com.onnyth.onnythserver.store.domain.model.CosmeticItem;
import com.onnyth.onnythserver.store.domain.model.CosmeticRarity;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CosmeticItemResponse(
        UUID id,
        String name,
        String description,
        String previewUrl,
        CosmeticCategory category,
        Integer price,
        CosmeticRarity rarity,
        Boolean isOwned,
        Boolean isEquipped
) {
    public static CosmeticItemResponse fromEntity(CosmeticItem item, boolean isOwned, boolean isEquipped) {
        return CosmeticItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .previewUrl(item.getPreviewUrl())
                .category(item.getCategory())
                .price(item.getPrice())
                .rarity(item.getRarity())
                .isOwned(isOwned)
                .isEquipped(isEquipped)
                .build();
    }
}
