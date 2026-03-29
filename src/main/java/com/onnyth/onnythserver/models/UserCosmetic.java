package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Records a user's ownership and equip status of a cosmetic item.
 * Unique constraint prevents duplicate purchases.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_cosmetics", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cosmetic_item_id"}))
public class UserCosmetic {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "cosmetic_item_id", nullable = false)
    private UUID cosmeticItemId;

    @Column(name = "purchased_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant purchasedAt = Instant.now();

    @Column(name = "is_equipped", nullable = false)
    @Builder.Default
    private Boolean isEquipped = false;
}
