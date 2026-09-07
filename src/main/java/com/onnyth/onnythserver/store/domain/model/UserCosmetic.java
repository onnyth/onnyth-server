package com.onnyth.onnythserver.store.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Records a user's ownership and equip status of a cosmetic item.
 * Unique constraint prevents duplicate purchases.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCosmetic {

    private UUID id;
    private UUID userId;
    private UUID cosmeticItemId;

    @Builder.Default
    private Instant purchasedAt = Instant.now();

    @Builder.Default
    private Boolean isEquipped = false;
}
