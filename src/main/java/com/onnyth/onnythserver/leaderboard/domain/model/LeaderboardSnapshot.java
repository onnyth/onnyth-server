package com.onnyth.onnythserver.leaderboard.domain.model;

import com.onnyth.onnythserver.models.StatDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Weekly snapshot of a user's leaderboard position among a friend group.
 * Used to calculate position changes (moved up/down) between weeks.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardSnapshot {

    private UUID id;
    private UUID userId;
    private UUID friendOwnerId;
    private int position;
    private long score;
    private LocalDate snapshotDate;
    private StatDomain category;
}
