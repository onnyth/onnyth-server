-- V9: Create leaderboard_snapshots table for weekly position tracking
CREATE TABLE leaderboard_snapshots (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL REFERENCES users(id),
    friend_owner_id   UUID NOT NULL REFERENCES users(id),
    position          INTEGER NOT NULL,
    score             BIGINT NOT NULL,
    snapshot_date     DATE NOT NULL,
    category          VARCHAR(30)
);

CREATE INDEX idx_leaderboard_snapshots_lookup
    ON leaderboard_snapshots(friend_owner_id, snapshot_date);

CREATE INDEX idx_leaderboard_snapshots_user
    ON leaderboard_snapshots(user_id, snapshot_date);
