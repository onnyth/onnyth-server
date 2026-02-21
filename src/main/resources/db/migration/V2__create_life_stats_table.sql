-- Migration: Create life_stats table for user stat tracking
-- Story 3 - Life Stats Input & Storage

CREATE TABLE life_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    category VARCHAR(30) NOT NULL,
    value INTEGER NOT NULL CHECK (value BETWEEN 1 AND 100),
    last_updated TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata TEXT,
    UNIQUE (user_id, category)
);

CREATE INDEX idx_life_stats_user_id ON life_stats(user_id);

COMMENT ON TABLE life_stats IS 'Stores individual life stat values per user per category';
COMMENT ON COLUMN life_stats.category IS 'One of: CAREER, WEALTH, FITNESS, EDUCATION, SOCIAL_INFLUENCE';
COMMENT ON COLUMN life_stats.value IS 'Stat value, range 1-100';
COMMENT ON COLUMN life_stats.metadata IS 'Optional JSON metadata for the stat entry';
