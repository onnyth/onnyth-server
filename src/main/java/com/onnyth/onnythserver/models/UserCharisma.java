package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregates a user's social presence metrics for the Charisma domain.
 * onnyth_profile_likes is a denormalized counter for fast reads (source of truth: profile_likes table).
 * Onnyth follower count is derived from the follows table.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_charisma", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserCharisma {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "onnyth_profile_likes", nullable = false)
    @Builder.Default
    private Integer onnythProfileLikes = 0;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "last_social_sync_at")
    private Instant lastSocialSyncAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
