-- V11: Create user_achievements table for tracking unlock status
CREATE TABLE user_achievements (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL REFERENCES users(id),
    achievement_id    UUID NOT NULL REFERENCES achievements(id),
    unlocked_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_id)
);

CREATE INDEX idx_user_achievements_user ON user_achievements(user_id);
