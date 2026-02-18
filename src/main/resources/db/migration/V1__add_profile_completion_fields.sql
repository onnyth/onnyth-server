-- Migration: Add profile completion fields to users table
-- Sprint 1 - Story 1.1: Profile Completion
-- Run this script in Supabase SQL Editor

-- Add profile_complete column
ALTER TABLE users
ADD COLUMN IF NOT EXISTS profile_complete BOOLEAN NOT NULL DEFAULT FALSE;

-- Add updated_at column
ALTER TABLE users
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- Update username column constraints (unique, max length 20)
ALTER TABLE users
ALTER COLUMN username TYPE VARCHAR(20);

-- Add unique constraint to username if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'users_username_key'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT users_username_key UNIQUE (username);
    END IF;
END $$;

-- Update full_name column constraints (max length 100)
ALTER TABLE users
ALTER COLUMN full_name TYPE VARCHAR(100);

-- Create index on username for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Comment on columns
COMMENT ON COLUMN users.profile_complete IS 'True when user has set username, full_name, and profile_pic';
COMMENT ON COLUMN users.updated_at IS 'Timestamp of last profile update';

