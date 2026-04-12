package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a "like" on a user's profile/ID card (not a post like).
 * Used for the Charisma domain score — tracks who liked whose profile.
 * Composite PK (liker_id, liked_id) prevents duplicate likes.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ProfileLikeId.class)
@Table(name = "profile_likes")
public class ProfileLike {

    @Id
    @Column(name = "liker_id", nullable = false)
    private UUID likerId;

    @Id
    @Column(name = "liked_id", nullable = false)
    private UUID likedId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
