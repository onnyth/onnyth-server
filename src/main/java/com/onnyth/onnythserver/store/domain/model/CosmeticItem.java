package com.onnyth.onnythserver.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A cosmetic item available for purchase in the store.
 * Items have a category, price (in XP), and rarity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosmeticItem {

    private UUID id;
    private String name;
    private String description;
    private String previewUrl;
    private CosmeticCategory category;
    private Integer price;

    @Builder.Default
    private CosmeticRarity rarity = CosmeticRarity.COMMON;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
