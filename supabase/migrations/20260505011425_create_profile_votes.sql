-- V23: Create profile_votes table for tracking per-user upvote/downvote on profiles

CREATE TABLE IF NOT EXISTS profile_votes (
                                             voter_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_upvote BOOLEAN NOT NULL,
    voted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (voter_id, target_id)
    );

CREATE INDEX IF NOT EXISTS idx_profile_votes_target ON profile_votes(target_id);
