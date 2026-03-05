package com.onnyth.onnythserver.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Weekly snapshot of a user's leaderboard position among a friend group.
 * Used to calculate position changes (moved up/down) between weeks.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leaderboard_snapshots")
public class LeaderboardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "friend_owner_id", nullable = false)
    private UUID friendOwnerId;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "score", nullable = false)
    private long score;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private StatCategory category;
}
