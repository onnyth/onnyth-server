-- Migration: Add registration_drafts table + extend users and user_charisma for multi-form registration

-- 1. Registration drafts table for crash-recoverable multi-step onboarding
CREATE TABLE IF NOT EXISTS public.registration_drafts (
    user_id       UUID PRIMARY KEY REFERENCES public.users(id) ON DELETE CASCADE,
    current_step  VARCHAR(30) NOT NULL DEFAULT 'PHONE',
    draft_data    JSONB NOT NULL DEFAULT '{}'::jsonb,
    version       INTEGER NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at    TIMESTAMPTZ NOT NULL DEFAULT (now() + interval '30 days'),

    CONSTRAINT chk_current_step CHECK (current_step IN (
        'PHONE', 'NAME', 'IMAGE', 'OCCUPATION', 'WEALTH',
        'PHYSIQUE', 'WISDOM', 'CHARISMA'
    ))
);

CREATE INDEX IF NOT EXISTS idx_registration_drafts_expires
    ON public.registration_drafts(expires_at);

-- Auto-update updated_at on modification
CREATE TRIGGER trg_registration_drafts_updated
    BEFORE UPDATE ON public.registration_drafts
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 2. Add phone and profile_type to users table
ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20),
    ADD COLUMN IF NOT EXISTS profile_type VARCHAR(10) DEFAULT 'personal';

-- Add check constraint for profile_type (only if not already present)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_profile_type'
    ) THEN
        ALTER TABLE public.users
            ADD CONSTRAINT chk_profile_type CHECK (profile_type IN ('personal', 'business'));
    END IF;
END $$;

-- 3. Add relationship_status and social_circle_size to user_charisma
ALTER TABLE public.user_charisma
    ADD COLUMN IF NOT EXISTS relationship_status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS social_circle_size INTEGER;
