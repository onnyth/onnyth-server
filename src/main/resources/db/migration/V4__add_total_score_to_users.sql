-- Migration: Add total_score to users table
-- Story 5 - Score Calculation

ALTER TABLE users ADD COLUMN total_score BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN users.total_score IS 'Persisted weighted life score, updated on every stat change';
