package com.onnyth.onnythserver.store.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
public class UserCosmeticEntity {

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
