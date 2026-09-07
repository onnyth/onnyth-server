package com.onnyth.onnythserver.friendship.adapter.out.persistence;

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
 * Represents a bidirectional friendship between two users.
 * Each friendship creates 2 rows: A→B and B→A for efficient querying.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "friend_id" }))
public class FriendshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "friend_id", nullable = false)
    private UUID friendId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
