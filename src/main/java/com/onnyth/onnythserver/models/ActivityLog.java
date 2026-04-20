package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

/**
 * Records a single activity log entry — a user completing an activity.
 * Tracks the XP earned and is used for cooldown validation.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "activity_type_id", nullable = false)
    private UUID activityTypeId;

    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned;

    @Column(name = "logged_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    @Builder.Default
    private Instant loggedAt = Instant.now();
}
