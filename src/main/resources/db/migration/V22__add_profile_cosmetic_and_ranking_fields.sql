-- V22: Add world rank, country rank, country, vote score, and active cosmetic columns to users table

ALTER TABLE users ADD COLUMN IF NOT EXISTS world_rank INT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS country_rank INT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS country VARCHAR(2);
ALTER TABLE users ADD COLUMN IF NOT EXISTS vote_score INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS active_background_color VARCHAR(7);
ALTER TABLE users ADD COLUMN IF NOT EXISTS active_frame_cosmetic_id UUID REFERENCES cosmetic_items(id) ON DELETE SET NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS active_background_cosmetic_id UUID REFERENCES cosmetic_items(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_users_total_score ON users(total_score DESC);
CREATE INDEX IF NOT EXISTS idx_users_country ON users(country);
