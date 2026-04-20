-- Migration: Add previous_value to life_stats and create history table
-- Story 4 - Stat Updates with History Tracking

-- Add previousValue column to life_stats
ALTER TABLE life_stats ADD COLUMN previous_value INTEGER;

-- Create history table for audit trail of stat changes
CREATE TABLE life_stat_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    category VARCHAR(30) NOT NULL,
    old_value INTEGER NOT NULL,
    new_value INTEGER NOT NULL,
    reason TEXT,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_life_stat_history_user ON life_stat_history(user_id);
CREATE INDEX idx_life_stat_history_user_category ON life_stat_history(user_id, category);

COMMENT ON TABLE life_stat_history IS 'Append-only audit log of stat changes';
COMMENT ON COLUMN life_stat_history.reason IS 'Optional user-provided reason for the change';
