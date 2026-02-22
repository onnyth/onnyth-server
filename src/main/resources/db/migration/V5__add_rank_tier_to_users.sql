-- V5: Add rank_tier column to users table for persistent rank display
ALTER TABLE users ADD COLUMN rank_tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE';
