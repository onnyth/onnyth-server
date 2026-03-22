package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks per-user achievement unlock status.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_achievements", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "achievement_id" }))
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "achievement_id", nullable = false)
    private UUID achievementId;

    @ColumnDefault("now()")
    @Column(name = "unlocked_at", nullable = false, updatable = false, insertable = false)
    private Instant unlockedAt;
}
