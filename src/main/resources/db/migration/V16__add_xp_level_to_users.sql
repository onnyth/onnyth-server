-- V16: Add xp and level columns to users table
ALTER TABLE users ADD COLUMN xp BIGINT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN level INTEGER NOT NULL DEFAULT 1;
