package com.onnyth.onnythserver.dto;

import com.onnyth.onnythserver.models.CosmeticCategory;
import com.onnyth.onnythserver.models.CosmeticItem;
import com.onnyth.onnythserver.models.CosmeticRarity;
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
