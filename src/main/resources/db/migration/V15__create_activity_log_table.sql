-- V15: Create activity_log table
CREATE TABLE activity_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id),
    activity_type_id    UUID NOT NULL REFERENCES activity_types(id),
    xp_earned           INTEGER NOT NULL,
    logged_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_log_user_id ON activity_log(user_id);
CREATE INDEX idx_activity_log_user_type_time ON activity_log(user_id, activity_type_id, logged_at);
