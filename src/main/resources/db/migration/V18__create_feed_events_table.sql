-- V18: Create feed_events table
CREATE TABLE feed_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    event_type      VARCHAR(30) NOT NULL,
    event_data      TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_feed_events_user_id ON feed_events(user_id);
CREATE INDEX idx_feed_events_created_at ON feed_events(created_at);
